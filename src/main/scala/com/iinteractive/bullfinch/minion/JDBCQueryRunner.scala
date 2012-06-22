package com.iinteractive.bullfinch.minion

import com.iinteractive.bullfinch._
import com.iinteractive.bullfinch.util.JSONResultSetWrapper
import java.sql.{Connection,PreparedStatement,ResultSet}
import net.liftweb.json._
import scala.collection.JavaConversions._
import scala.collection.mutable.Buffer

/**
 * Represents a request from Kestrel.
 */
case class Request(
  response_queue: Option[String],
  statement: String,
  params: Option[Seq[String]]
)

/**
 * Represents one of the statements available for remote execution.
 */
case class Statement(
  sql: String,
  params: Option[Seq[String]]
)

/**
 * Minion for executing SQL from a list of pre-defined statements. Leverages
 * QueueMonitor and JDBCBased.
 *
 * The queries may optionally specify a list of expected parameters by
 * supplying their types.  The acceptable types are:
 * 
 * * BOOLEAN
 * * INTEGER
 * * NUMBER
 * * STRING
 *
 * The queue monitor expects configuration like:
 *
 *{{{
 * {
 *   "workers" : [
 *   {
 *     "name" : "Query Runner",
 *     "worker_class" : "com.iinteractive.bullfinch.minion.JDBCQueryRunner",
 *     "worker_count" : 2,
 *     "options"  : {
 *       "kestrel_host" : "127.0.0.1",
 *       "kestrel_port" : 22133,
 *       "subscribe_to" : "test-net-kestrel",
 *       "timeout" : 10000,
 *       "connection" : {
 *         "driver" : "com.mysql.jdbc.Driver",
 *         "dsn" : "jdbc:mysql://localhost/test",
 *         "uid" : "root",
 *         "validation" : "SELECT 1"
 *       },
 *       "statements" : {
 *         "aQuery": {
 *           "sql": "INSERT INTO table (foo, bar) VALUES (?, ?)",
 *           "params": [ "STRING", "INTEGER" ]
 *         },
 *         "anotherQuery": {
 *           "sql": "SELECT foo FROM table"
 *         }
 *       }
 *     }
 *   }  
 * }
 *}}}
 *
 * The request sent in via kestrel should have a statement (optionally) a
 * list of parameters and/or a response queue.
 *
 * The request (from the Kestrel Queue) should look like:
 *
 *{{{
 *{
 *  "statement": "qQuery",
 *  "params": [ "hello", 12 ]
 *}
 *}}}
 *
 * or, if you don't have params and/or want to see the response
 *
 *{{{
 *{
 *  "statement": "qnotherQuery",
 *  "response_queue": "boasdasdasd"
 *}
 *}}}
 */
class JDBCQueryRunner(config: Option[Map[String,Any]]) extends Minion(config) with QueueMonitor with JDBCBased {

  implicit val formats = DefaultFormats
  
  val statementConfig = config.get("statements").asInstanceOf[Map[String,Any]]
  println(statementConfig.values.size)
  val statements = statementConfig.mapValues { more =>
    val newmap = more.asInstanceOf[Map[String,Object]]
    val sql = newmap.get("sql") match {
      case Some(sql) => sql.asInstanceOf[String]
      case None => throw new RuntimeException("JDBC Query Runner worker statements must have SQL")
    }
    val params: Option[Seq[String]] = newmap.get("params").asInstanceOf[Option[Seq[String]]]
    Statement(
      sql = sql,
      params = params
    )
  }

  /**
   * Encode a ResultSet as JSON and send it to the specified queue.
   */
  def encodeResponse(queue: String, resultSet: ResultSet) {
    new JSONResultSetWrapper(resultSet) foreach { message =>
      sendMessage(queue, message)
    }
  }

  /**
   * Given a Request and Statement, sanity check, execute, apply parameters
   * and respond if necessary. Leverages JDBCBased to handle Connection and
   * PreparedStatement management.
   */
  def bindAndExecuteQuery(request: Request, statement: Statement) {
    withConnection { conn =>
      withStatement(conn, statement.sql) { prep =>
        // Check if this statement requires parameters
        statement.params map { statementParams =>
          request.params match {
            // Since we require params, did we get them from the request?
            case Some(requestParams) if requestParams.size == statementParams.size => {
              applyParams(prep, statementParams, requestParams)
              prep.execute
              // Only send the resultset back if we have a response queue
              request.response_queue map { queueName =>
                prep.getResultSet match {
                  case rs: ResultSet=> {
                    try {
                      encodeResponse(queueName, rs)
                    } finally {
                      rs.close
                    }
                  }
                  case _            => // Do nothing
                }
              }
            }
            case Some(requestParams) if requestParams.size != statementParams.size => throw new IllegalArgumentException("Incorrect number of parameters for " + request.statement)
            case None => throw new IllegalArgumentException("Parameters required for statement " + request.statement)
          }
        }
      }
    }
  }
  
  /**
   * Matches up the pre-determined parameter list for a statement with user-
   * supplied arguments and binds them to a PreparedStatement. Throws an
   * IllegalArgumentException if it encounters strangeness.
   */
  def applyParams(statement: PreparedStatement, statementParams: Iterable[String], requestParams: Iterable[String]) {
    
    // A bit lengthy, but we want to iterate over the params in pairs and
    // we need the index for the statements set so we'll end up with
    // Tuple2(Tuple2(PARAMTYPE,PARAM),INDEX))
    // Also, view is used to prevent the creation of intermediary lists.
    statementParams.view.zip(requestParams).view.zipWithIndex.foreach { bits =>
      val pair = bits._1
      val index = bits._2 + 1
      pair._1 match {
        case "BOOLEAN"  => statement.setBoolean(index, pair._2.toBoolean)
        case "NUMBER"   => statement.setDouble(index, pair._2.toDouble)
        case "INTEGER"  => statement.setInt(index, pair._2.toInt)
        case "STRING"   => statement.setString(index, pair._2)
        case _          => {
          throw new IllegalArgumentException("Don't understand param type " + pair._1)
        }
      }
    }
  }

  override def configure {
    super.configure
    log.debug("Configure in JDBCQueryRunner")
  }
  
  /**
   * Override of KestrelBased's `handle` function, executing the request.
   */
  override def handle(json: String) {

    log.info("Handling request...")

    var rs: Option[ResultSet] = None

    // Try and turn the JSON into a Request
    val request = try {
      Some(parse(json).extract[Request])
    } catch {
      case ex: Exception => {
        log.error("Unable to parse request", ex);
        None
      }
    }
    
    request match {
      case Some(r) => {

        // Make sure we have a statement to execute before we go to any trouble
        val statement = statements.get(r.statement)
        statement match {
          case Some(statement) => bindAndExecuteQuery(r, statement)
          // Got no request, bitch if we can
          case None            => r.response_queue map { rqueue => sendMessage(rqueue, """{ "ERROR":"Unable to parse request!" }""") }
          // If we have a response queue then cap things off with an EOF
          r.response_queue map { rqueue => sendMessage(rqueue, """{ "EOF":"EOF" }""") }
        }
      }
      // Crap, this means we'll just ignore it completely. Log an error.
      case None => log.error("No request found, doing nothing. Item will be ignored and confirmed.")
    }    
  }
}
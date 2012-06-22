package com.iinteractive.bullfinch.minion

import com.iinteractive.bullfinch._
import com.iinteractive.bullfinch.util.JSONResultSetWrapper
import java.sql.{Connection,PreparedStatement,ResultSet}
import net.liftweb.json._
import scala.collection.JavaConversions._

/**
 * Represents one of the statements available for remote execution.
 */
case class Statement(
  sql: String,
  params: Option[Seq[String]]
)

case class SQLRequest(
  response_queue: Option[String],
  statements: Seq[String],
  params: Seq[Seq[String]]
)

object SQLRequest {
  implicit val formats = DefaultFormats
}

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

  val statements = statementConfig.mapValues { more =>
    val newmap = more.asInstanceOf[Map[String,Object]]
    val sql = newmap.get("sql") match {
      case Some(sql) => sql.asInstanceOf[String]
      case None => throw new RuntimeException("JDBC Query Runner worker statements must have SQL")
    }

    Statement(
      sql = sql,
      params = newmap.get("params").asInstanceOf[Option[Seq[String]]]
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
  def bindAndExecuteQuery(responseQueue: Option[String], requestParams: Seq[String], statement: Statement) {
    withConnection { conn =>
      withStatement(conn, statement.sql) { prep =>
        // Check if this statement requires parameters
        statement.params map { statementParams =>
          applyParams(prep, statementParams, requestParams)
        }
        prep.execute
        // Only send the resultset back if we have a response queue
        println(responseQueue)
        responseQueue match {
          case Some(queueName) => {
            prep.getResultSet match {
              case rs: ResultSet=> {
                try {
                  encodeResponse(queueName, rs)
                } finally {
                  rs.close
                }
              }
              case _            => {
                log.debug("No ResultSet found, not sending a response.")
              }
            }
          }
          case None => {
            log.debug("No response queue, not sending a response")
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

    // Try and turn the JSON into a Request
    val request = try {
      parse(json).extract[SQLRequest]
    } catch {
      // Crap, this means we'll just ignore it completely. Log an error.
      case ex: Exception => {
        log.error("Unable to parse request", ex);
        log.error("Doing nothing. Item will be ignored and confirmed.")
        return
      }
    }
    
    // Find any invalid queries
    val invalidStatements = request.statements map { rs =>
      statements.get(rs)
    } filterNot { x =>
      x.isDefined
    }
    if(!invalidStatements.isEmpty) {
      val invalidQueries = invalidStatements.flatten.mkString(",")
      log.error("Request contain invalid queries: %s" + invalidQueries)
      request.response_queue map { rqueue => sendMessage(rqueue, "{ \"ERROR\":\"Invalid queries: " + invalidQueries + "\" }") }
      // Abort, we don't want to run this if one of the queries is bad!
      return
    }

    request.statements zip request.params map { stateParamPair =>
      val statement = statements.get(stateParamPair._1).get
      bindAndExecuteQuery(request.response_queue, stateParamPair._2, statement)
    }
    // If we have a response queue then cap things off with an EOF
    request.response_queue map { rqueue => sendMessage(rqueue, """{ "EOF":"EOF" }""") }
  }
}
package com.iinteractive.bullfinch.minion

import com.iinteractive.bullfinch._
import com.iinteractive.bullfinch.util.JSONResultSetWrapper
import java.sql.{Connection,PreparedStatement,ResultSet}
import java.util.{ArrayList,LinkedHashMap}
import net.liftweb.json._
import scala.collection.JavaConversions._
import scala.collection.mutable.Buffer

case class Request(
  responseQueue: Option[String],
  statement: String,
  params: Option[Seq[String]]
)

case class Statement(
  sql: String,
  params: Option[Buffer[String]]
)

class JDBCQueryRunner(config: Option[Map[String,Any]]) extends Minion(config) with QueueMonitor with JDBCBased {

  implicit val formats = DefaultFormats

  val statementConfig = config.get("statements").asInstanceOf[LinkedHashMap[String,Object]].toMap
  val statements = statementConfig.mapValues { more =>
    val newmap = more.asInstanceOf[LinkedHashMap[String,Object]].toMap
    val sql = newmap.get("sql") match {
      case Some(sql) => sql.asInstanceOf[String]
      case None => throw new RuntimeException("JDBC Query Runner worker statements must have SQL")
    }
    val params: Option[Buffer[String]] = newmap.get("params") match {
      case Some(p) => Some(asScalaBuffer[String](p.asInstanceOf[ArrayList[String]]))
      case None => None
    }
    Statement(
      sql = sql,
      params = params
    )
  }

  def bindAndExecuteQuery(conn: Connection, request: Request): Either[String,PreparedStatement] = {
    
    val prepStatement = statements.get(request.statement) match {
      // First we verify that we have a statement by that name
      case Some(more) => {
        try {
          val prep = conn.prepareStatement(more.sql)
          // Check if this statement requires parameters
          more.params match {
            case Some(params) => {
              request.params match {
                // Since we require params, did we get them from the request?
                case Some(requestParams) if requestParams.size == params.size => {
                  applyParams(prep, params, requestParams)
                  Right(prep)
                }
                case Some(requestParams) if requestParams.size != params.size => Left("Incorrect number of parameters for " + request.statement)
                case None => Left("Parameters required for statement " + request.statement)
              }
            }
            // No params, so no reason to do anything
            case None => Right(prep)
          }
        } catch {
          case e: Exception => {
            log.error("Exception preparing and executing statement: ", e)
            Left("Exception: " + e.getMessage)
          }
        }
      }
      case None => Left("Unknown statement: " + request.statement)
    }
    
    // If we get a proper statement, execute it before returning
    prepStatement match {
      case Right(x) => {
        x.execute()
      }
      case _ => // Nothing
    }
    prepStatement
  }
  
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
    log.debug("Configure in JDBC")
  }
  
  // XXX http://jim-mcbeath.blogspot.com/2008/09/creating-control-constructs-in-scala.html
  def withConnection(body: (Connection) => Unit) {

    val conn = pool.getConnection
    try {
      body(conn)
      // Verify this isn't eating all exceptions
    } finally {
      if(conn != null) {
        try {
          conn.close
        } catch {
          case ex: Exception => log.error("Error releasing database connection: ", ex)
        }
      }
    }
  }

  override def handle(json: String) {

    log.info("Handling request...")

    val request = parse(json).extract[Request]
    // var conn: Option[Connection] = None
    var ps: Option[PreparedStatement] = None
    var rs: Option[ResultSet] = None

    try {
      withConnection { conn =>
        val result = bindAndExecuteQuery(conn, request)
        result match {
          case Right(statement) => {
            // Things bound and executed
            ps = Some(statement)

            request.responseQueue match {
              case Some(rqueue) => {
                // If we have a response queue then we need to send some sort
                // of response, even if it's just the EOF
                statement.getResultSet match {
                  case resultSet: ResultSet => {
                    // Since we have a response queue and a resultset, we need
                    // to encode it and send it back
                    rs = Some(resultSet)
                    val wrapper = new JSONResultSetWrapper(resultSet = resultSet)
                    wrapper.foreach { message =>
                      sendMessage(rqueue, message)
                    }
                  }
                  case null => // no resultset
                }
                // Cap things off with the EOF
                sendMessage(rqueue, """{ "EOF":"EOF" }""")
              }
              case None => // No response to send
            }
          }
          case Left(err) => // XXX Got an error
        }
      }
      // Attempt to bind and execute the query
    // } catch {
    //   asdasd XXXX
    } finally {
      // Close up all the things that need to be closed
      // conn match {
      //   case Some(c) => try { c.close } catch { case e: Exception => } // HONEY BADGER
      //   case None => //
      // }
      rs match {
        case Some(r) => try { r.close } catch { case e: Exception => } // HONEY BADGER
        case None => //
      }
      ps match {
        case Some(p) => try { p.close } catch { case e: Exception => } // HONEY BADGER
        case None => //
      }
    }
  }
}
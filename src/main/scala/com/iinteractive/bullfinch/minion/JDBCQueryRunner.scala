package com.iinteractive.bullfinch.minion

import com.codahale.jerkson.AST._
import com.codahale.jerkson.Json._
import com.codahale.jerkson.JsonSnakeCase
import com.iinteractive.bullfinch._
import java.util.{ArrayList,LinkedHashMap}
import scala.collection.JavaConversions._

@JsonSnakeCase
case class Request(
  responseQueue: Option[String],
  statement: String,
  params: Option[Seq[String]]
)

class JDBCQueryRunner(config: Option[Map[String,Any]]) extends Minion(config) with QueueMonitor with JDBCBased {

  val statementConfig = config.get("statements").asInstanceOf[LinkedHashMap[String,Object]].toMap
  val statements = statementConfig.mapValues { more =>
    val newmap = more.asInstanceOf[LinkedHashMap[String,Object]].toMap
    val sql = newmap.get("sql") match {
      case Some(sql) => sql.asInstanceOf[String]
      case None => throw new RuntimeException("JDBC Query Runner worker statements must have SQL")
    }
    val params = newmap.get("params") match {
      case Some(p) => { asScalaBuffer(p.asInstanceOf[ArrayList[String]]) }
      case None => None
    }
    Map(
      "sql"   -> sql,
      "params"-> params
    )
  }
  log.error("STATEMENTS: " + statements)

  override def configure {
    super.configure
    log.debug("Configure in JDBC")
  }
  
  override def handle(request: String) {

    val req = parse[Request](request)

    log.info("Handling request!")    
  }
}
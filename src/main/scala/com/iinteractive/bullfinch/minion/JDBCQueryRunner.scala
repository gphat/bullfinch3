package com.iinteractive.bullfinch.minion

import com.codahale.jerkson.AST._
import com.codahale.jerkson.Json._
import com.codahale.jerkson.JsonSnakeCase
import com.iinteractive.bullfinch._

@JsonSnakeCase
case class Request(
  responseQueue: Option[String],
  statement: String,
  params: Option[Seq[String]]
)

class JDBCQueryRunner(config: Option[Map[String,Any]]) extends Minion(config) with QueueMonitor with JDBCBased {

  override def configure {
    super.configure
    log.debug("Configure in JDBC")
  }
  
  override def handle(request: String) {

    val req = parse[Request](request)

    log.info("Handling request!")    
  }
}
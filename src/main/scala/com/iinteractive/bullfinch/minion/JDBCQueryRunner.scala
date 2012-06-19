package com.iinteractive.bullfinch.minion

import com.codahale.jerkson.AST._
import com.codahale.jerkson.Json._
import com.codahale.jerkson.JsonSnakeCase
import com.iinteractive.bullfinch._
import com.twitter.grabbyhands._

@JsonSnakeCase
case class Request(
  responseQueue: Option[String],
  statement: String,
  params: Option[Seq[String]]
)

class JDBCQueryRunner(config: Option[Map[String,Any]]) extends Minion(config) with QueueMonitor {

  override def configure {
    super.configure
    log.debug("Configure in JDBC")
  }
  
  override def handle(request: String) {

    val req = parse[Request](request)

    val w = req.responseQueue match {
      case Some(name) => {
        log.debug("Connection to " + host + ":" + port + ", queue " + name)
        val c = new Config()
        c.addServer(host + ":" + port.toString)
        c.addQueue(name)
        Some(new GrabbyHands(c))
      }
      case None => None
    }

    log.info("Handling request!")
    
    w match {
      case Some(c) => {
        sendMessage(c, req.responseQueue.get, """{ "EOF":"EOF" }""")
        c.join
      }
      case None => // Do nothing!
    }
  }
}
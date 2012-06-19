package com.iinteractive.bullfinch.minion

import java.util.concurrent.TimeUnit
import com.codahale.jerkson.AST._
import com.codahale.jerkson.Json._
import com.codahale.jerkson.JsonSnakeCase
import com.twitter.grabbyhands.Write

@JsonSnakeCase
case class Request(
  responseQueue: Option[String],
  statement: String,
  params: Option[Seq[String]]
)

trait QueueMonitor extends KestrelBased {

  def handle(request: Request)

  override def configure {
    super.configure
    log.info("Configure in KestrelBased")
  }
  
  override def run {
    while(this.shouldContinue) {

      log.error("Opening item from queue")

      val write = new Write("""{"response_queue": "foo", "statement": "bar"}""")
      client.getSendQueue(queue).put(write)
      write.awaitWrite
      
      val foo = new String(client.getRecvQueue(queue).poll(timeout, TimeUnit.MILLISECONDS).array)
      if(foo != null) {
        log.error("Got: " + foo)
        process(foo)
      }
    }
  }
  
  def process(request: String) {

    val req = parse[Request](request)
    handle(req)

    // If we got a responseQueue, cap it off
    req.responseQueue match {
      case Some(queue) => sendMessage(queue, """{ "EOF":"EOF" }""")
      case None => // Do nothing!
    }
  }
}
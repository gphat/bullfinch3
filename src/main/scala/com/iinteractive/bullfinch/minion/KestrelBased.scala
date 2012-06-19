package com.iinteractive.bullfinch.minion

import com.twitter.grabbyhands._
import com.iinteractive.bullfinch.Minion
import scala.collection.JavaConversions._

trait KestrelBased extends Minion {
  
  val host = getConfigOrElse[String]("kestrel_host", "127.0.0.1")
  val port = getConfigOrElse[Int]("kestrel_port", 22133)
  val queue = getConfigOrElse[String]("subscribe_to", "bullfinch")
  val timeout = getConfigOrElse[Int]("timeout", 10000)
  lazy val client = {
    log.debug("Connection to " + host + ":" + port + ", queue " + queue)
    val c = new Config()
    c.addServer(host + ":" + port.toString)
    c.addQueue(queue)
    new GrabbyHands(c)
  }

  override def configure {
    super.configure
    log.info("Configure in KestrelBased")
  }
  
  override def cancel {
    super.cancel
    
    client.join
  }
  
  def sendMessage(writeClient: GrabbyHands, queue: String, message: String) {

    val write = new Write(queue)
    writeClient.getSendQueue(queue).put(write)
    write.awaitWrite

    log.debug("Wrote: " + message)
  }
}
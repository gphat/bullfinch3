package com.iinteractive.bullfinch.minion

import com.twitter.grabbyhands._
import com.iinteractive.bullfinch.Minion
import scala.collection.JavaConversions._

trait KestrelBased extends Minion {
  
  val host = getConfigOrElse[String]("kestrel_host", "127.0.0.1")
  val port = getConfigOrElse[Int]("kestrel_port", 22133)
  val queue = getConfigOrElse[String]("subscribe_to", "bullfinch")
  val client = {
    val c = new Config()
    c.addServer(host + ":" + port.toString)
    c.addQueue(queue)
    new GrabbyHands(c)
  }

  override def configure {
    super.configure
    println("Configure in KestrelBased")
  }
}
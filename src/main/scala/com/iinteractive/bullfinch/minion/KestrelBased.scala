package com.iinteractive.bullfinch.minion

import com.twitter.grabbyhands._
import com.iinteractive.bullfinch.Minion

trait KestrelBased extends Minion {
  
  override def configure(config: Map[String,Any]) = {
    super.configure(config)
    println("Configure in KestrelBased")
    // val host = config.get("kestrel_host") match {
    //   case Some(x) => x.asInstanceOf[String]
    //   case None => //
    // }
    
    val c = new Config()
    c.addServer("127.0.0.1:22133")
    c.addQueue("test-net-kestrel")
    val grabby = new GrabbyHands(c)
  }
}
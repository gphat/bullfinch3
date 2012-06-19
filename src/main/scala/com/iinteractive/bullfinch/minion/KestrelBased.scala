package com.iinteractive.bullfinch.minion

import com.twitter.grabbyhands._

trait KestrelBased {
  
  def configure(config: Map[String,Any]) = {

    // val host = config.get("kestrel_host") match {
    //   case Some(x) => x.asInstanceOf[String]
    //   case None => //
    // }
    
    val config = new Config()
    config.addServer("127.0.0.1:22133")
    config.addQueue("test-net-kestrel")
    val grabby = new GrabbyHands(config)
  }
}
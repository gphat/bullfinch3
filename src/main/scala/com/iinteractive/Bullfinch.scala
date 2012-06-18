package com.iinteractive

import com.iinteractive.bullfinch._
import grizzled.slf4j.Logging
import java.net.{MalformedURLException,URL};

object Bullfinch {
  
  def main(args: Array[String]) {
    
    val configs = args filter { arg =>
      val url = try {
        Some(new URL(arg))
      } catch {
        case e: MalformedURLException => None
      }
      url.isDefined
    } map { arg =>
      new URL(arg)
    }

    // Check this post-url-load, as even if we DO get some in args it's better
    // to filter out the bad ones first.
    if(configs.size < 1) {
      error("Must provide a config file!")
      return
    }
    
    val boss = new Boss(configs = configs)
  }
}
package com.iinteractive

import com.iinteractive.bullfinch._
import com.codahale.logula.Logging
import java.net.{MalformedURLException,URL};
import org.apache.log4j.Level

object Bullfinch {

  Logging.configure { log =>
    log.registerWithJMX = true

    log.level = Level.INFO
    // log.loggers("com.myproject.weebits") = Level.OFF

    log.console.enabled = true
    log.console.threshold = Level.WARN

    // log.file.enabled = true
    // log.file.filename = "/var/log/myapp/myapp.log"
    // log.file.maxSize = 10 * 1024 // KB
    // log.file.retainedFiles = 5 // keep five old logs around

    // syslog integration is always via a network socket
    // log.syslog.enabled = true
    // log.syslog.host = "syslog-001.internal.example.com"
    // log.syslog.facility = "local3"
  }
  
  def main(args: Array[String]) {
    
    val configs = args flatMap { arg =>
      try {
        Some(new URL(arg))
      } catch {
        case e: MalformedURLException => None
      }
    }

    // Check this post-url-load, as even if we DO get some in args it's better
    // to filter out the bad ones first.
    if(configs.size < 1) {
      error("Must provide a config file!")
      return
    }
    
    val boss = new Boss(urls = configs)
    boss.start
    Thread.sleep(1000)
    boss.stop
  }
}
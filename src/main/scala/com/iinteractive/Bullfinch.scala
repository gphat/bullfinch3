package com.iinteractive

import com.iinteractive.bullfinch._
import com.codahale.logula.Logging
import java.net.{MalformedURLException,URL};
import org.apache.log4j.Level

/**
 * Entry point. Using `main` here will let you start up an instance of
 * Bullfinch.
 */
object Bullfinch {

  Logging.configure { log =>
    log.registerWithJMX = true

    log.level = Level.DEBUG
    // log.loggers("com.myproject.weebits") = Level.OFF

    log.console.enabled = true
    log.console.threshold = Level.DEBUG

    // log.file.enabled = true
    // log.file.filename = "/var/log/myapp/myapp.log"
    // log.file.maxSize = 10 * 1024 // KB
    // log.file.retainedFiles = 5 // keep five old logs around

    // syslog integration is always via a network socket
    // log.syslog.enabled = true
    // log.syslog.host = "syslog-001.internal.example.com"
    // log.syslog.facility = "local3"
  }
  
  /**
   * Start an instance of bullfinch.  Requires at least one valid URL for
   * a config file.  If any of the URLs are invalid then that config file
   * will be eliminated and not included in future checks. XXX Fix this?
   */
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
    // XXX Change this
    boss.start
    Thread.sleep(2000)
    boss.stop
  }
}
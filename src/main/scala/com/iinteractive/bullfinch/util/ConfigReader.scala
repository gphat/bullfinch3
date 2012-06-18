package com.iinteractive.bullfinch.util

import com.codahale.jerkson.AST._
import com.codahale.jerkson.Json._
import com.iinteractive.Bullfinch._
import grizzled.slf4j.Logging
import java.io.InputStreamReader
import java.net.URL

object ConfigReader extends Logging {
  
  def read(configs: Seq[URL]): Seq[Configuration] = {
    
    configs.flatMap { config =>
      try {
        debug("Attempting to read '" + config + "'")

        val conn = config.openConnection
        val lastModified = conn.getLastModified
        
        debug("Last modified: " + lastModified)
        
        Some(parse[Configuration](new java.io.InputStreamReader(config.openStream)))
      } catch {
        // We don't really care why the config failed, we just report
        // the errorand move on
        case e => {
          error("Failed to parse config '" + config + "', stacktrace follows")
          e.printStackTrace
          None
        }
      }
    }
  }
}
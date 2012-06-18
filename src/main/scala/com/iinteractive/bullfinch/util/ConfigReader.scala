package com.iinteractive.bullfinch.util

import com.codahale.jerkson.AST._
import com.codahale.jerkson.Json._
import com.codahale.jerkson.JsonSnakeCase
import grizzled.slf4j.Logging
import java.io.InputStreamReader
import java.net.URL

object ConfigReader extends Logging {

  @JsonSnakeCase
  case class Config(
    configRefreshSeconds: Int = 300,
    workers: Option[Seq[Map[String,Any]]]
  )
  case class ConfigSource(
    config: Config,
    lastModified: Long,
    url: URL
  )
  
  def read(configs: Seq[URL]): Seq[ConfigSource] = {
    
    configs.flatMap { url =>
      try {
        debug("Attempting to read '" + url + "'")

        val conn = url.openConnection
        val lastModified = conn.getLastModified
        
        debug("Last modified: " + lastModified)
        
        val config = parse[Config](new java.io.InputStreamReader(url.openStream))
        
        Some(ConfigSource(
          config = config,
          lastModified = lastModified,
          url = url
        ))
      } catch {
        // We don't really care why the config failed, we just report
        // the erro rand move on
        case e => {
          error("Failed to parse config '" + url + "', stacktrace follows")
          e.printStackTrace
          None
        }
      }
    }
  }  
}
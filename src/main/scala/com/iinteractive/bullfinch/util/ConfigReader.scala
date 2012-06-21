package com.iinteractive.bullfinch.util

import com.codahale.logula.Logging
import java.io.{BufferedReader,InputStreamReader}
import java.net.URL
import net.liftweb.json._

object ConfigReader extends Logging {

  implicit val formats = DefaultFormats

  case class ConfigSource(
    configRefreshSeconds: Int,
    config: Map[String,Any],
    lastModified: Long,
    url: URL
  )
  
  def read(configs: Seq[URL]): Seq[ConfigSource] = {
    
    configs.flatMap { url =>
      try {
        log.debug("Attempting to read '" + url + "'")

        val conn = url.openConnection
        val lastModified = conn.getLastModified
        
        log.debug("Last modified: " + lastModified)
        
        val buff = new BufferedReader(new InputStreamReader(url.openStream, "UTF-8"))
        var line = buff.readLine
        var sb = new StringBuffer
        while(line != null) {
          sb.append(line)
          sb.append("\n")
          line = buff.readLine
        }

        println(sb.toString)
        val config = parse(sb.toString).asInstanceOf[JObject].values
        println(config.get("config_refresh_seconds"))
        
        Some(ConfigSource(
          configRefreshSeconds = config.getOrElse("config_refresh_seconds", 300),
          // configRefreshSeconds = config find {
          //   case JField("config_refresh_seconds", _) => true
          //   case _ => false
          // } match {
          //   case Some(x) => x.extract[Int]
          //   case None => 300
          // },
          config = config,
          lastModified = lastModified,
          url = url
        ))
      } catch {
        // We don't really care why the config failed, we just report
        // the erro rand move on
        case e => {
          e.printStackTrace
          log.error("Failed to parse config '" + url + "', stacktrace follows", e)
          None
        }
      }
    }
  } 
}
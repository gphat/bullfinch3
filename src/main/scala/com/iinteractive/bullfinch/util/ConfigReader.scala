package com.iinteractive.bullfinch.util

import com.codahale.logula.Logging
import java.io.{BufferedReader,InputStreamReader}
import java.net.URL
import net.liftweb.json._
import scala.collection.JavaConversions._

/**
 * Handle the reading of JSON config files via  URL
 */
object ConfigReader extends Logging {

  implicit val formats = DefaultFormats

  case class ConfigSource(
    configRefreshSeconds: Int,
    config: JValue,
    lastModified: Long,
    url: URL
  )
  
  /**
   * Read in a list of config files via URL. Each URL is read and parsed. If
   * any individual config file fails then it will be ignored. XXX Fix this, partial read or something.
   */
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
          line = buff.readLine
        }

        val config = parse(sb.toString)
        
        Some(ConfigSource(
          configRefreshSeconds = (config \ "config_refresh_seconds").extract[BigInt].intValue,
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
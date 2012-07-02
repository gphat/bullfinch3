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
    config: JValue,
    lastModified: Long
  )
  
  /**
   * Read in a list of config files via URL. Each URL is read and parsed.
   * Successful config files will have a `Some[ConfigSource]` while failed
   * ones will have a `None` as a member of the returned Map, keyed by
   * the string representations of the URL.
   *
   * This use of `Option` was chosen to make it possible to reload configs
   * in the future as well as to allow merging of "last-seen" config
   * information if a subsequent re-read fails.
   */
  def read(configs: Seq[URL]): Map[URL,ConfigSource] = {
    
    Map(configs map { url =>

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

      url -> ConfigSource(
        config = config,
        lastModified = lastModified
      )
    }: _*)
  }

  /**
   * Returns a boolean that signals if the provided configuration is out
   * of date.
   */
  def isOutdated(oldConfigs: Map[URL,ConfigSource]): Boolean = {

    val urls = oldConfigs.keys.toSeq
    val newConfigs = read(urls)

    val result = try {
      urls.map { url =>
        val oldC = oldConfigs.get(url).get // We know these exist
        val newC = newConfigs.get(url).get // already, so .get em

        // Check if config was modified more recently
        if(newC.lastModified > oldC.lastModified) {
          true
        } else {
          false
        }
      }
    } catch {
      case ex: Exception => {
        log.error("Failed to open & parse configs", ex)
        return false
      }
    }

    return result.contains(true)
  }
}
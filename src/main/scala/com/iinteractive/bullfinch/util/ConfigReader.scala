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
  def read(configs: Seq[URL]): Map[URL,Option[ConfigSource]] = {
    
    Map(configs map { url =>
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

        url -> Some(ConfigSource(
          config = config,
          lastModified = lastModified
        ))
      } catch {
        // We don't really care why the config failed, we just report
        // the erro rand move on
        case e => {
          e.printStackTrace
          log.error("Failed to parse config '" + url + "', stacktrace follows", e)
        }
        url -> None
      }
    }: _*)
  }

  /**
   * Returns a `Tuple2` containing a boolean that signals if the returned
   * config is different from the one passed in and a (possible new) map
   * of `ConfigSource`s.  The result of this call can be used to create a
   * new `Boss` instance.
   */
  def reRead(oldConfigs: Map[URL,Option[ConfigSource]]): (Boolean,Map[URL,Option[ConfigSource]]) = {

    var changed = false

    val urls = oldConfigs.keys.toSeq
    val newConfigs = read(urls)

    val result = urls.map { url =>
      val oldC = oldConfigs.get(url).get // We know these exist
      val newC = newConfigs.get(url).get // already, so .get em

      oldC match {
        case None if(newC.isDefined) => {
          // New config is Some, use it
          changed = true
          newC
        }
        case Some(conf) => {
          // Check if config was modified more recently
          newC match {
            case Some(c) if c.lastModified > conf.lastModified => {
              changed = true
              newC
            }
            case None => oldC // Use the old one
          }
        }
        case _ => oldC // Use the old one
      }
    }

    val resultMap = Map(urls zip result: _*)
    (changed -> resultMap)
  }
}
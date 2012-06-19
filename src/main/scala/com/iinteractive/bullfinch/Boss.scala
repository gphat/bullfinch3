package com.iinteractive.bullfinch

import com.iinteractive.bullfinch.util.ConfigReader
import grizzled.slf4j.Logging
import java.net.URL
import scala.collection.JavaConversions._

class Boss(urls: Seq[URL]) extends Logging {
  
  val configs = ConfigReader.read(urls)
  
  case class WorkerConfig(
    name: String,
    className: String,
    count: Option[Int],
    options: Option[Map[String,Int]]
  )
  
  def start() {
    
    prepareWorkers
  }
  
  def stop() {
    
  }
  
  private def prepareWorkers {
    
    val workerConfigs = configs.flatMap { cs =>
      cs.config.workers match {
        case Some(w) => {
          w.map { wc =>
            WorkerConfig(
              name      = wc.get("name").get.asInstanceOf[String],
              className = wc.get("worker_class").get.asInstanceOf[String],
              count     = wc.get("worker_count").asInstanceOf[Option[Int]],
              options   = wc.get("options").asInstanceOf[Option[java.util.LinkedHashMap[String,Int]]] match {
                // This bit of pattern matching is to conver the LinkedHashMap
                // to a scala map for convenience.
                case Some(m) => Some(m.toMap)
                case None => None
              }
            )
          }
        }
        case None => None
      }
    }
    
    workerConfigs.map { wc =>
      val ins = Class.forName(wc.className).getDeclaredConstructor(
        classOf[Option[Map[String,Any]]]
      ).newInstance(wc.options).asInstanceOf[Minion]
      println(ins)
      ins.configure
    }
  }
}
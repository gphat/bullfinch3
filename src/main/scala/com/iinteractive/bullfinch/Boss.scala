package com.iinteractive.bullfinch

import com.iinteractive.bullfinch.util.ConfigReader
import grizzled.slf4j.Logging
import java.net.URL

class Boss(urls: Seq[URL]) extends Logging {
  
  val configs = ConfigReader.read(urls)
  
  case class WorkerConfig(
    name: String,
    className: String,
    count: Some[Int],
    options: Some[Map[String,Any]]
  )
  
  def start() {
    
    prepareWorkers
  }
  
  def stop() {
    
  }
  
  private def prepareWorkers {
    
    val workerConfigs = configs.map { cs =>
      cs.config.workers match {
        case Some(w) => {
          w.map { wc =>
            WorkerConfig(
              name      = wc.get("name").get.asInstanceOf[String],
              className = wc.get("worker_class").get.asInstanceOf[String],
              count     = wc.get("worker_count").asInstanceOf[Some[Int]],
              options   = wc.get("options").asInstanceOf[Some[Map[String,Any]]]
            )
          }
        }
        case None => None
      }
    }
    
    workerConfigs.map { wc =>
      // Load it! XXX
    }
  }
}
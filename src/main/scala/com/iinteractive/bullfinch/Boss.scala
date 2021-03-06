package com.iinteractive.bullfinch

import com.iinteractive.bullfinch.util.ConfigReader
import com.codahale.logula.Logging
import java.net.URL
import java.util.concurrent.TimeoutException
import net.liftweb.json._
import scala.collection.JavaConversions._

class Boss(urls: Seq[URL]) extends Logging {
  
  implicit val formats = DefaultFormats
  
  var configs = ConfigReader.read(urls)
  var workers = prepareWorkers
  
  case class WorkerConfig(
    name: String,
    className: String,
    count: BigInt,
    options: Option[Map[String,Any]]
  )
  
  def start() {

    log.info("Starting workers")
    workers.foreach { worker =>
      log.info("Starting {}", worker._1)
      worker._3.start()
    }
  }
  
  def stop() {
    
    log.info("Cancelling minions")
    
    workers.foreach { worker =>
      worker._2.asInstanceOf[Minion].cancel
    }
    
    workers.foreach { worker =>
      worker._3.join()
    }
  }

  def waitForConfigChanges {
    while(true) {
      Thread.sleep(60 * 1000)
      throw new TimeoutException
    }
  }

  private def prepareWorkers: Seq[(String,Minion,Thread)] = {
    
    log.debug("Preparing workers")
    // Iterate over each config file and create the workers contained therein.
    // Use flatMap so that the lists are flattened down into a single list,
    // rather than a list of lists.
    val wcs = configs.values
    val workerConfigs = wcs flatMap { cs =>

      val workers = (cs.config \\ "workers").values.asInstanceOf[List[Map[String,Any]]]
      workers.map { wc =>
        WorkerConfig(
          name      = wc.get("name").get.asInstanceOf[String],
          className = wc.get("worker_class").get.asInstanceOf[String],
          count     = wc.get("worker_count").asInstanceOf[BigInt],
          options   = wc.get("options").asInstanceOf[Option[Map[String,Any]]]
        )
      }
    }

    workerConfigs.flatMap { wc =>
      val ins = Class.forName(wc.className).getDeclaredConstructor(
        classOf[Option[Map[String,Any]]]
      ).newInstance(wc.options).asInstanceOf[Minion]

      log.info("Starting {} instances of {}", wc.count, wc.name)
      1.to(wc.count.intValue) map { count =>
        (wc.name, ins, new Thread(ins))
      }
    } toSeq
  }
}
package com.iinteractive.bullfinch

import com.iinteractive.bullfinch.util.ConfigReader
import com.codahale.logula.Logging
import java.net.URL
import net.liftweb.json._
import scala.collection.JavaConversions._

class Boss(urls: Seq[URL]) extends Logging {
  
  implicit val formats = DefaultFormats
  
  var configs = ConfigReader.read(urls)
  var workers = prepareWorkers
  
  case class WorkerConfig(
    name: String,
    className: String,
    count: Option[BigInt],
    options: Option[Map[String,Any]]
  )
  
  def start() {

    log.info("Starting workers")
    workers.foreach { worker =>
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
  
  private def prepareWorkers: Seq[(String,Minion,Thread)] = {
    
    log.debug("Preparing workers")
    // Iterate over each config file and create the workers contained therein.
    // Use flatMap so that the lists are flattened down into a single list,
    // rather than a list of lists.
    val workerConfigs = configs.flatMap { cs =>

      val workers = (cs.config \\ "workers").values.asInstanceOf[List[Map[String,Any]]]
      workers.map { wc =>
        WorkerConfig(
          name      = wc.get("name").get.asInstanceOf[String],
          className = wc.get("worker_class").get.asInstanceOf[String],
          count     = wc.get("worker_count").asInstanceOf[Option[BigInt]],
          options   = wc.get("options").asInstanceOf[Option[Map[String,Any]]]
        )
      }
    }

    // XXX Honor count!!
    workerConfigs.map { wc =>
      val ins = Class.forName(wc.className).getDeclaredConstructor(
        classOf[Option[Map[String,Any]]]
      ).newInstance(wc.options).asInstanceOf[Minion]
      ins.configure // XXX Remove
      (wc.name, ins, new Thread(ins))
    }
  }
}
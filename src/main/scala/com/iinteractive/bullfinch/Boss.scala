package com.iinteractive.bullfinch

import com.iinteractive.bullfinch.util.ConfigReader
import com.codahale.logula.Logging
import java.net.URL
import scala.collection.JavaConversions._

class Boss(urls: Seq[URL]) extends Logging {
  
  var configs = ConfigReader.read(urls)
//  var workers = prepareWorkers
  
  case class WorkerConfig(
    name: String,
    className: String,
    count: Option[Int],
    options: Option[Map[String,Int]]
  )
  
  def start() {

    log.info("Starting workers")
    // workers.foreach { worker =>
    //   worker._3.start()
    // }
  }
  
  def stop() {
    
    log.info("Cancelling minions")
    
    // workers.foreach { worker =>
    //   worker._2.asInstanceOf[Minion].cancel
    // }
    // 
    // workers.foreach { worker =>
    //   worker._3.join()
    // }
  }
  
  // private def prepareWorkers: Seq[(String,Minion,Thread)] = {
  //   
  //   log.debug("Preparing workers")
  //   val workerConfigs = configs.flatMap { cs =>
  //     cs.config.workers match {
  //       case Some(w) => {
  //         w.map { wc =>
  //           WorkerConfig(
  //             name      = wc.get("name").get.asInstanceOf[String],
  //             className = wc.get("worker_class").get.asInstanceOf[String],
  //             count     = wc.get("worker_count").asInstanceOf[Option[Int]],
  //             options   = wc.get("options").asInstanceOf[Option[java.util.LinkedHashMap[String,Int]]] match {
  //               // This bit of pattern matching is to convert the LinkedHashMap
  //               // to a scala map for convenience.
  //               case Some(m) => Some(m.toMap)
  //               case None => None
  //             }
  //           )
  //         }
  //       }
  //       case None => None
  //     }
  //   }
  //   
  //   workerConfigs.map { wc =>
  //     val ins = Class.forName(wc.className).getDeclaredConstructor(
  //       classOf[Option[Map[String,Any]]]
  //     ).newInstance(wc.options).asInstanceOf[Minion]
  //     ins.configure
  //     (wc.name, ins, new Thread(ins))
  //   }
  // }
}
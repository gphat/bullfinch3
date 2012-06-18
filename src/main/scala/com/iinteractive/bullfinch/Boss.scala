package com.iinteractive.bullfinch

import com.iinteractive.bullfinch.util.ConfigReader
import grizzled.slf4j.Logging
import java.net.URL

class Boss(urls : Seq[URL]) extends Logging {
  
  val configs = ConfigReader.read(urls)
  
  def start() {
    
    configs map { conf =>
      println(conf)
    }
  }
  
  def stop() {
    
  }
}
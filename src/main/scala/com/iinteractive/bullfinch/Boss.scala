package com.iinteractive.bullfinch

import com.iinteractive.bullfinch.util.ConfigReader
import grizzled.slf4j.Logging
import java.net.URL

class Boss(configs : Seq[URL]) extends Logging {
  
  val configURLs = ConfigReader.read(configs)
  val configTimestamps : Seq[Long] = Seq()
  
  def restart() {
    
  }
  
  def start() {
    
  }
  
  def stop() {
    
  }
}
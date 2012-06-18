package com.iinteractive.bullfinch

import grizzled.slf4j.Logging
import java.net.URL;

class Boss(configs : Seq[URL]) extends Logging {
  
  val configURLs = configs
  val configTimestamps : Seq[Long] = Seq()
  
  def start() {
    
  }
  
  def stop() {
    
  }
}
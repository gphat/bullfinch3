package com.iinteractive.bullfinch.minion

import com.iinteractive.bullfinch.Minion

class JDBCTableScanner extends Minion with Runnable with KestrelBased {
  
  override def configure(config: Map[String,Any]) {
    super.configure(config)
    println("Configure in JDBC Scanner")
  }
  
  def run = {
  }
}
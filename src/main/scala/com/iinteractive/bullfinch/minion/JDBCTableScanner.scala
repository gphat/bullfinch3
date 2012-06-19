package com.iinteractive.bullfinch.minion

import com.iinteractive.bullfinch.Minion

class JDBCTableScanner(config: Option[Map[String,Any]]) extends Minion(config) with Runnable with KestrelBased {
  
  override def configure {
    super.configure
    println("Configure in JDBC Scanner")
    println(config)
  }
  
  def run = {
    println("Running TableScanner")
  }
}
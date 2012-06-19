package com.iinteractive.bullfinch.minion

import com.iinteractive.bullfinch._

class JDBCQueryRunner(config: Option[Map[String,Any]]) extends Minion(config) with QueueMonitor {

  override def configure {
    super.configure
    log.debug("Configure in JDBC")
  }
  
  override def handle(request: Request) {
    
  }
}
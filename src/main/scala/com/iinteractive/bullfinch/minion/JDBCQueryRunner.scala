package com.iinteractive.bullfinch.minion

import com.iinteractive.bullfinch.Minion

class JDBCQueryRunner(config: Option[Map[String,Any]]) extends Minion(config) with KestrelBased {

  override def configure {
    super.configure
    log.debug("Configure in JDBC")
  }

  def run = {

    while(this.shouldContinue) {
      log.error("table scanner")
      Thread.sleep(500)
    }
  }  
}
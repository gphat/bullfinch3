package com.iinteractive.bullfinch.minion

trait QueueMonitor extends KestrelBased {

  def handle(json: String)

  override def configure {
    super.configure
    log.info("Configure in QueueMonitor")
  }
  
  override def run {
    while(this.shouldContinue) {

      log.error("Opening item from queue") // XXX wrong log level

      val resp = getMessage(queue)
      resp match {
        case Some(x) => {
          // XXX Maybe wrap this with a try
          handle(x)
          confirm(queue)
        }
        case None => // Continue looping, no data that time
      }
      
      Thread.sleep(250) // XXX remove this
    }
  }  
}
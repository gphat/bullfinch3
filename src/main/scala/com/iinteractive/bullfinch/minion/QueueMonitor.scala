package com.iinteractive.bullfinch.minion

trait QueueMonitor extends KestrelBased {

  def handle(request: String)

  override def configure {
    super.configure
    log.info("Configure in KestrelBased")
  }
  
  override def run {
    while(this.shouldContinue) {

      log.error("Opening item from queue")

      val resp = getMessage(queue)
      resp match {
        case Some(x) => {
          process(x)
          confirm(queue)
        }
        case None => // Continue looping, no data that time
      }
      
      Thread.sleep(250)
    }
  }
  
  def process(request: String) {

    handle(request)
  }
}
package com.iinteractive.bullfinch.minion

/**
 * Leverages KestrelBased to read messages from a queue and dispatch them to
 * a `handle` method.
 */
trait QueueMonitor extends KestrelBased {

  /**
   * Method that must be implemented by consumers. 
   */
  def handle(json: String)

  override def configure {
    super.configure
    log.info("Configure in QueueMonitor")
  }
  
  /**
   * Implementation of the run function that handles reading from a 
   */
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
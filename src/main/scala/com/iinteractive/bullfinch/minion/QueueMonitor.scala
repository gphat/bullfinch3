package com.iinteractive.bullfinch.minion

import java.util.concurrent.TimeUnit
import com.twitter.grabbyhands.Write

trait QueueMonitor extends KestrelBased {

  def handle(request: String)

  override def configure {
    super.configure
    log.info("Configure in KestrelBased")
  }
  
  override def run {
    while(this.shouldContinue) {

      log.error("Opening item from queue")

      // val write = new Write("""{"response_queue": "foo", "statement": "bar"}""")
      // client.getSendQueue(queue).put(write)
      // write.awaitWrite
      
      val msg = client.getRecvTransQueue(queue).poll(timeout, TimeUnit.MILLISECONDS)
      if(msg != null) {
        log.error("Got: " + msg)
        process(new String(msg.message.array))
        msg.close()
      }
      
      Thread.sleep(250)
    }
  }
  
  def process(request: String) {

    handle(request)
  }
}
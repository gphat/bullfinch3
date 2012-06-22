package com.iinteractive.bullfinch.minion

import com.iinteractive.bullfinch.Minion
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit
import net.spy.memcached.{MemcachedClient,OperationTimeoutException}
import scala.collection.JavaConversions._

trait KestrelBased extends Minion {
  
  val host = getConfigOrElse[String]("kestrel_host", "127.0.0.1")
  val port = getConfigOrElse[BigInt]("kestrel_port", 22133).intValue
  val queue = getConfigOrElse[String]("subscribe_to", "bullfinch")
  val timeout = getConfigOrElse[BigInt]("timeout", 10000).intValue
  lazy val client = new MemcachedClient(
    new InetSocketAddress(host, port)
  )

  override def configure {
    super.configure
    log.debug("Configure in KestrelBased")
  }
  
  override def cancel {
    super.cancel
    
    client.shutdown(10, TimeUnit.SECONDS)
  }
  
  def confirm(queue: String) {
    
    log.debug("Closing " + queue)
    client.get(queue + "/close")
  }
  
  def getMessage(queue: String, tout: Int = timeout): Option[String] = {
    
    try {
      val resp = client.get(queue + "/open/t=" + tout).asInstanceOf[String]
      if(resp == null) {
        None
      } else {
        log.debug("Opened transaction with " + queue)
        Some(resp)
      }
    } catch {
      case ote: OperationTimeoutException => None
      case e: Exception => {
        log.error("Caught an exception:", e)
        None
      }
    }
  }
  
  def sendMessage(queue: String, message: String) {

    client.set(queue, 0, message)
    log.debug("Wrote: " + message + " to " + queue)
  }
}
package com.iinteractive.bullfinch.minion

import com.iinteractive.bullfinch.Minion
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit
import net.spy.memcached.{MemcachedClient,OperationTimeoutException}
import scala.collection.JavaConversions._

/**
 * Minion trait for talking to a Kestrel instance.
 */
trait KestrelBased extends Minion {
  
  val host = getConfigOrElse[String]("kestrel_host", "127.0.0.1")
  val port = getConfigOrElse[BigInt]("kestrel_port", 22133).intValue
  val queue = getConfigOrElse[String]("subscribe_to", "bullfinch")
  val timeout = getConfigOrElse[BigInt]("timeout", 10000).intValue
  
  // We want to allow our tests to set a mock memcached client, so this
  // variable is here to allow us to have it set externally.  In normal
  // use the lazy call to getConnection will result in a lazily created
  // client using the above arguments.
  var protoClient: Option[MemcachedClient] = None
  lazy val client = getConnection

  def getConnection: MemcachedClient = {
    protoClient match {
      case Some(c)=> c
      case None   => new MemcachedClient(
        new InetSocketAddress(host, port)
      )
    }
  }
  
  override def configure {
    super.configure
    log.debug("Configure in KestrelBased")
  }
  
  /**
   * Minion's `cancel` implementation. Closes the memcached client.
   */
  override def cancel {
    super.cancel
    
    client.shutdown(1, TimeUnit.SECONDS) /// XXX Maybe change this?
  }
  
  /**
   * Close an open transaction with Kestrel.
   */
  def confirm(queue: String) {
    
    log.debug("Closing " + queue)
    client.get(queue + "/close")
  }
  
  /**
   * Block for up to `timeout` milliseconds looking for a message on the
   * specified queue. If no message is found before the timeout is exceeded,
   * returns a None.
   */
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
  
  /**
   * Send a message to the queue.
   */
  def sendMessage(queue: String, message: String) {

    client.set(queue, 0, message)
    log.debug("Wrote: " + message + " to " + queue)
  }
}
package com.iinteractive.bullfinch

import com.codahale.logula.Logging
import scala.collection.JavaConversions._

/**
 * Base class for workers.
 */
abstract class Minion(protected val config: Option[Map[String,Any]]) extends Runnable with Logging {

  var cancelled = false

  // XXX remove this
  def configure {
    log.debug("Configure in Minion")
  }
  
  /**
   * Convenience function for retrieving config values in the style of
   * `getOrElse`.
   */
  def getConfigOrElse[A](key: String, default: A): A = {

    config match {
      case Some(c)  => c.getOrElse(key, default).asInstanceOf[A]
      case None     => default
    }
  }
  
  /**
   * Notify this worker that it needs to end as soon as possible.
   */
  def cancel = {
    log.error("Got cancel")
    this.cancelled = true
  }
  
  /**
   * Function workers use in their run loops to verify if they should continue
   * running.
   */
  def shouldContinue: Boolean = {
    return !Thread.currentThread().isInterrupted() && !cancelled
  }
}
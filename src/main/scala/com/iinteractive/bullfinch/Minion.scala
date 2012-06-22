package com.iinteractive.bullfinch

import com.codahale.logula.Logging
import scala.collection.JavaConversions._

abstract class Minion(protected val config: Option[Map[String,Any]]) extends Runnable with Logging {

  var cancelled = false

  // XXX remove this
  def configure {
    log.debug("Configure in Minion")
  }
  
  def getConfigOrElse[A](key: String, default: A): A = {

    config match {
      case Some(c)  => c.getOrElse(key, default).asInstanceOf[A]
      case None     => default
    }
  }
  
  def cancel = {
    log.error("Got cancel")
    this.cancelled = true
  }
  
  def shouldContinue: Boolean = {
    return !Thread.currentThread().isInterrupted() && !cancelled
  }
}
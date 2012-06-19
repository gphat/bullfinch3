package com.iinteractive.bullfinch

class Minion(protected val config: Option[Map[String,Any]]) {

  var cancelled = false
  
  def configure {
    println("Configure in Minion")
  }
  
  def getConfigOrElse[A](key: String, default: A): A = {

    config match {
      case Some(c) => c.getOrElse(key, default).asInstanceOf[A]
      case None => {
        default
      }
    }
  }
  
  def cancel = {
    this.cancelled = true
  }
  
  def shouldContinue: Boolean = {
    return !Thread.currentThread().isInterrupted() && !cancelled
  }
}
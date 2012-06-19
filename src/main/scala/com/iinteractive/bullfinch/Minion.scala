package com.iinteractive.bullfinch

class Minion(protected val config: Option[Map[String,Any]]) {

  var cancelled = false
  
  def configure {
    println("Configure in Minion")
  }
  
  def cancel = {
    this.cancelled = true
  }
  
  def shouldContinue: Boolean = {
    return !Thread.currentThread().isInterrupted() && !cancelled
  }
}
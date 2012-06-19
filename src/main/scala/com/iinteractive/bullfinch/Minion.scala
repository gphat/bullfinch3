package com.iinteractive.bullfinch

class Minion(config: Map[String,Any]) {

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
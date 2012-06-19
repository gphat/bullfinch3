package com.iinteractive.bullfinch

class Minion {
  
  var cancelled = false
  
  def configure(config: Map[String,Any]) = {
    println("Configure in Minion")
  }
  
  def cancel = {
    this.cancelled = true
  }
  
  def shouldContinue: Boolean = {
    return !Thread.currentThread().isInterrupted() && !cancelled
  }
}
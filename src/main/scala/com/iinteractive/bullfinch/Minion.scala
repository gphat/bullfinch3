package com.iinteractive.bullfinch

trait Minion {
  
  var cancelled = false
  
  def configure(config: Map[String,Any]) {
    println("configure in Minion")
  }
  
  def cancel = {
    this.cancelled = true
  }
  
  def shouldContinue: Boolean = {
    return !Thread.currentThread().isInterrupted() && !cancelled
  }
}
package com.iinteractive

import com.codahale.jerkson.JsonSnakeCase
import com.iinteractive.bullfinch._
import grizzled.slf4j.Logging
import java.net.{MalformedURLException,URL};

object Bullfinch {
  
  @JsonSnakeCase
  case class Workers(
    name: String,
    workerClass: String
  )
  
  case class Configuration(
    config_refresh_seconds: Int = 300,
    workers: Seq[Map[String,Any]]
  )

  def main(args: Array[String]) {
    
    val configs = args flatMap { arg =>
      try {
        Some(new URL(arg))
      } catch {
        case e: MalformedURLException => None
      }
    }

    // Check this post-url-load, as even if we DO get some in args it's better
    // to filter out the bad ones first.
    if(configs.size < 1) {
      error("Must provide a config file!")
      return
    }
    
    val boss = new Boss(configs = configs)
  }
}
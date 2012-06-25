package com.iinteractive.bullfinch

import org.specs2.mutable._

import com.iinteractive.Bullfinch
import com.iinteractive.bullfinch.util.ConfigReader

class ConfigurationSpec extends Specification {
  
  "The ConfigReader" should {
    "Return a configuration for a single" in {
      val url1 = this.getClass.getClassLoader.getResource("config.json")
      val configs = ConfigReader.read(Seq(url1))
      configs.size mustEqual 1
    }

    "Return a configuration for each config" in {
      val url1 = this.getClass.getClassLoader.getResource("config.json")
      val url2 = this.getClass.getClassLoader.getResource("small.json")
      val configs = ConfigReader.read(Seq(url1, url2))
      configs.size mustEqual 2
    }

    "Have sane configuration" in {
      val url1 = this.getClass.getClassLoader.getResource("config.json")
      val url2 = this.getClass.getClassLoader.getResource("small.json")
      val configs = ConfigReader.read(Seq(url1, url2))
      configs foreach { config =>
        config.configRefreshSeconds must be_>=(300)
      }
      1 mustEqual 1 // Junk, the above will blow up if it's wrong
    }

    "Deal with broken configs" in {
      val url1 = this.getClass.getClassLoader.getResource("config.json")
      val url2 = this.getClass.getClassLoader.getResource("bad.json")
      val configs = ConfigReader.read(Seq(url1, url2))
      configs.size mustEqual 1
    }

    "Be sane if all configs are bad" in {
      val url = this.getClass.getClassLoader.getResource("bad.json")
      val configs = ConfigReader.read(Seq(url))
      configs.size mustEqual 0
    }
  }
}
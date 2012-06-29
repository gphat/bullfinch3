package com.iinteractive.bullfinch

import org.specs2.mutable._

import net.liftweb.json.JsonAST._
import com.iinteractive.Bullfinch
import com.iinteractive.bullfinch.util.ConfigReader
import com.iinteractive.bullfinch.util.ConfigReader._
import java.net.URL

class ConfigurationSpec extends Specification {
  
  "The ConfigReader" should {
    "Return a configuration for a single" in {
      val url1 = this.getClass.getClassLoader.getResource("config.json")
      val configs = ConfigReader.read(Seq(url1))
      configs.size mustEqual 1
    }

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
      val configs = ConfigReader.read(Seq(url1, url2)).values
      1 must beEqualTo(1)
    }

    "Die with broken configs" in {
      val url1 = this.getClass.getClassLoader.getResource("config.json")
      val url2 = this.getClass.getClassLoader.getResource("bad.json")
      ConfigReader.read(Seq(url1, url2)) must throwA[Exception]
    }

    "Die if all configs are bad" in {
      val url = this.getClass.getClassLoader.getResource("bad.json")
      ConfigReader.read(Seq(url)) must throwA[Exception]
    }

    "Pick up a changed config" in {
      val config = this.getClass.getClassLoader.getResource("config.json")
      val configs = Map(config -> ConfigSource(
        lastModified = 0, // a bogus date, fs will be newer
        config = JNothing // Doesn't matter
      ))
      ConfigReader.isOutdated(configs) must beEqualTo(true)
    }

    "Ignore an unchanged config" in {
      val url1 = this.getClass.getClassLoader.getResource("config.json")
      val configs = ConfigReader.read(Seq(url1))
      ConfigReader.isOutdated(configs) must beEqualTo(false)
    }
  }
}
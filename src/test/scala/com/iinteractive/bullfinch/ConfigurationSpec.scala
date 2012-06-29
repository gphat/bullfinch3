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
      configs foreach { config =>
        config must beSome
      }
      1 must beEqualTo(1)
    }

    "Deal with broken configs" in {
      val url1 = this.getClass.getClassLoader.getResource("config.json")
      val url2 = this.getClass.getClassLoader.getResource("bad.json")
      val configs = ConfigReader.read(Seq(url1, url2))
      configs.size mustEqual 2
      val sources = configs.values.toSeq
      sources(0) must beSome // This one is good
      sources(1) must beNone // This one is bad
    }

    "Deal with missing configs" in {
      val url1 = this.getClass.getClassLoader.getResource("config.json")
      val url2 = new URL("file://foo/bar/baz/foo.json")
      val configs = ConfigReader.read(Seq(url1, url2))
      configs.size mustEqual 2
      val sources = configs.values.toSeq
      sources(0) must beSome // This one is good
      sources(1) must beNone // This one is bad
    }

    "Be sane if all configs are bad" in {
      val url = this.getClass.getClassLoader.getResource("bad.json")
      val configs = ConfigReader.read(Seq(url))
      configs.size mustEqual 1
    }

    "Pick up previously unavailable config" in {

      val config = this.getClass.getClassLoader.getResource("config.json")
      val configs = Map(config -> None)
      val result = ConfigReader.reRead(configs)

      result._1 should beEqualTo(true)
      result._2.get(config) must beSome
    }

    "Ignore a currently unavailable config" in {

      val config = new URL("file://foo/bar/gaz/foo.json")
      val configs = Map(config -> Some(ConfigSource(
        lastModified = 300,
        config = JNothing
      )))
      val result = ConfigReader.reRead(configs)

      result._1 should beEqualTo(false)
      result._2.get(config) must beSome
    }

    "Pick up a changed config" in {

      val config = this.getClass.getClassLoader.getResource("config.json")
      val configs = Map(config -> Some(ConfigSource(
        lastModified = 0, // a bogus date, fs will be newer
        config = JNothing // Doesn't matter
      )))
      val result = ConfigReader.reRead(configs)

      result._1 should beEqualTo(true)
      result._2.get(config) must beSome
      val workers = (result._2.get(config).get.get.config \\ "workers").values.asInstanceOf[List[Map[String,Any]]]
      workers.size must beGreaterThan(1)
    }

  }
}
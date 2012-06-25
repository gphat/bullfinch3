package com.iinteractive.bullfinch.minion

import org.specs2.mutable._
import com.iinteractive.bullfinch.Minion

class TestMinion(config: Option[Map[String,Any]]) extends Minion(config) {
  
  def run {
    
  }
}

class MinionSpec extends Specification {

  "Minion" should {

    "Be creatable" in {
      
      val m = new TestMinion(None)
      m must beAnInstanceOf[Minion]
    }

    "handle None as config" in {
      
      val m = new TestMinion(None)
      m.getConfigOrElse[String]("foobar", "baz") must beEqualTo("baz")
      m.getConfigOrElse[Int]("foobar", 1) must beEqualTo(1)
    }

    "handle Some as config" in {
      
      val m = new TestMinion(Some(Map("foobar" -> "baz")))
      m.getConfigOrElse[String]("foobar", "gorch") must beEqualTo("baz")
    }
    
    "handle cancellation" in {
      
      val m = new TestMinion(None)
      m.shouldContinue must beEqualTo(true)
      m.cancel
      m.shouldContinue must beEqualTo(false)
    }
  }
}
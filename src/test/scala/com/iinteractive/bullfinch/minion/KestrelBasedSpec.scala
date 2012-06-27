package com.iinteractive.bullfinch.minion

import org.specs2.mutable._
import com.iinteractive.bullfinch.Minion

class TestKestrelMinion(config: Option[Map[String,Any]]) extends Minion(config) with KestrelBased {
  def run {
    
  }
}

class KestrelBasedSpec extends Specification {

  "KestrelBased" should {
    "succeed with no config" in {
       new TestKestrelMinion(config = None)
       1 must beEqualTo(1)
     }
  }
}
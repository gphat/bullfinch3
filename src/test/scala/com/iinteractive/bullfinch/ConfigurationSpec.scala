package com.iinteractive.bullfinch

import org.specs2.mutable._

import com.iinteractive.Bullfinch

class ConfigurationSpec extends Specification {

  "The Boss" should {
    "fail if there are no config urls" in {
      Bullfinch.main(Seq().toArray) must throwA[RuntimeException]
    }

    "fail if there are all bad config urls" in {
      Bullfinch.main(Seq("crap").toArray) must throwA[RuntimeException]
    }
    
    "succeed with real urls" in {
      Bullfinch.main(Seq("http://www.example.com").toArray)
      1 mustEqual 1 // Junk, we'll throw an exception if the above fails
    }
  }
}
package com.iinteractive.bullfinch

import org.specs2.mutable._

import com.iinteractive.Bullfinch
import com.iinteractive.bullfinch.util.ConfigReader

class BossSpec extends Specification {

  "The Boss" should {
    "fail if there are no config urls" in {
      Bullfinch.main(Seq().toArray) must throwA[RuntimeException]
    }

    "fail if there are all bad config urls" in {
      Bullfinch.main(Seq("crap").toArray) must throwA[RuntimeException]
    }  
  }
}
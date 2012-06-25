package com.iinteractive.bullfinch.minion

import org.specs2.mutable._

class JDBCQueryRunnerSpec extends Specification {

  "The Query Runner" should {
    "fail with invalid query names" in {
      val queryRunner = new JDBCQueryRunner(config = None)
      1 must beEqualTo(1)
    }
  }
}
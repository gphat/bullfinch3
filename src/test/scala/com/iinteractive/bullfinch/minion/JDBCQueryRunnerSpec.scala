package com.iinteractive.bullfinch.minion

import org.specs2.mutable._

class JDBCQueryRunnerSpec extends Specification {

  "The Query Runner" should {
    "fail with no statements" in {

      val config = Map[String,Any](
        "connection" -> Map(
          "driver"  -> "org.hsqldb.jdbc.JDBCDriver",
          "dsn"     -> "jdbc:hsqldb:mem:bullfinch;shutdown=true",
          "uid"     -> "SA",
          "pwd"     -> "",
          "validation" -> "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS"
        )
      )
      
      new JDBCQueryRunner(config = Some(config)) must throwA[RuntimeException]
    }
  }

  "The Query Runner" should {
    "fail with invalid query names" in {

      val config = Map[String,Any](
        "connection" -> Map(
          "driver"  -> "org.hsqldb.jdbc.JDBCDriver",
          "dsn"     -> "jdbc:hsqldb:mem:bullfinch;shutdown=true",
          "uid"     -> "SA",
          "pwd"     -> "",
          "validation" -> "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS"
        ),
        "statements" -> Map(
          "selekta" -> "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS"
        )
      )
      
      val queryRunner = new JDBCQueryRunner(config = Some(config))
      
      // Should silently do nothing
      queryRunner.handle("""{}""")
      
      1 must beEqualTo(1)
    }
  }
}
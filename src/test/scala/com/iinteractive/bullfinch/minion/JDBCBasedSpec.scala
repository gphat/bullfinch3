package com.iinteractive.bullfinch.minion

import org.specs2.mutable._
import com.iinteractive.bullfinch.Minion

class TestJDBCMinion(config: Option[Map[String,Any]]) extends Minion(config) with JDBCBased {
  def run {
    
  }
}

class JDBCBasedSpec extends Specification {

  "The JDBCBased" should {
    "fail with no confg" in {
      new JDBCQueryRunner(config = None) must throwA[RuntimeException]
    }
    
    "fail with no connection" in {
      val config = Map[String,Any]()
      new JDBCQueryRunner(config = Some(config)) must throwA[RuntimeException]
    }

    "fail with no driver" in {
      val config = Map[String,Any]("connection" -> Map[String,Any]())
      new JDBCQueryRunner(config = Some(config)) must throwA[RuntimeException]
    }

    "fail with no dsn" in {
      val config = Map[String,Any](
        "connection" -> Map("driver" -> "org.hsqldb.jdbc.JDBCDriver")
      )
      new JDBCQueryRunner(config = Some(config)) must throwA[RuntimeException]
    }

    "fail with no uid" in {
      val config = Map[String,Any](
        "connection" -> Map(
          "driver"  -> "org.hsqldb.jdbc.JDBCDriver",
          "dsn"     -> "jdbc:hsqldb:mem:mymemdb"
        )
      )
      new JDBCQueryRunner(config = Some(config)) must throwA[RuntimeException]
    }

    "fail with no validation" in {
      val config = Map[String,Any](
        "connection" -> Map(
          "driver"  -> "org.hsqldb.jdbc.JDBCDriver",
          "dsn"     -> "jdbc:hsqldb:mem:mymemdb",
          "uid"     -> "SA"
        )
      )
      new JDBCQueryRunner(config = Some(config)) must throwA[RuntimeException]
    }

    "succeeds with all config info" in {
      val config = Map[String,Any](
        "connection" -> Map(
          "driver"  -> "org.hsqldb.jdbc.JDBCDriver",
          "dsn"     -> "jdbc:hsqldb:mem:mymemdb",
          "uid"     -> "SA",
          "validation" -> "SELECT current_timestamp FROM public.TEST_TABLE"
        )
      )
      new JDBCQueryRunner(config = Some(config)) must throwA[RuntimeException]
    }
  }
}
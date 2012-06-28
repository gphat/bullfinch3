package com.iinteractive.bullfinch.minion

import org.specs2.mutable._
import com.iinteractive.bullfinch.Minion

class TestJDBCMinion(config: Option[Map[String,Any]]) extends Minion(config) with JDBCBased {
  def run {
    
  }
  
  def testMethod = {
    withConnection(Some(false)) { conn =>
      val sql = "CREATE TABLE TEST_TABLE (an_int INTEGER, a_float FLOAT, a_bool BOOLEAN, a_string VARCHAR(32))"
      withStatement(conn, sql) { statement =>
        statement.execute
      }
    }
  }
}

class JDBCBasedSpec extends Specification {

  "The JDBCBased" should {
    "fail with no confg" in {
       new TestJDBCMinion(config = None) must throwA[RuntimeException]
     }
     
     "fail with no connection" in {
       val config = Map[String,Any]()
       new TestJDBCMinion(config = Some(config)) must throwA[RuntimeException]
     }
     
     "fail with no driver" in {
       val config = Map[String,Any]("connection" -> Map[String,Any]())
       new TestJDBCMinion(config = Some(config)) must throwA[RuntimeException]
     }
     
     "fail with no dsn" in {
       val config = Map[String,Any](
         "connection" -> Map[String,Any]("driver" -> "org.hsqldb.jdbc.JDBCDriver")
       )
       new TestJDBCMinion(config = Some(config)) must throwA[RuntimeException]
     }
     
     "fail with no uid" in {
       val config = Map[String,Any](
         "connection" -> Map[String,Any](
           "driver"  -> "org.hsqldb.jdbc.JDBCDriver",
           "dsn"     -> "jdbc:hsqldb:mem:bullfinch;shutdown=true"
         )
       )
       new TestJDBCMinion(config = Some(config)) must throwA[RuntimeException]
     }
     
     "fail with no validation" in {
       val config = Map[String,Any](
         "connection" -> Map[String,Any](
         "driver"     -> "org.hsqldb.jdbc.JDBCDriver",
         "dsn"        -> "jdbc:hsqldb:mem:bullfinch;shutdown=true",
         "uid"        -> "SA"
         )
       )
       new TestJDBCMinion(config = Some(config)) must throwA[RuntimeException]
     }

    "succeeds with all config info" in {
      val config = Map[String,Any](
        "connection" -> Map[String,Any](
          "driver"  -> "org.hsqldb.jdbc.JDBCDriver",
          "dsn"     -> "jdbc:hsqldb:mem:bullfinch;shutdown=true",
          "uid"     -> "SA",
          "pwd"     -> "",
          "validation" -> "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS"
        )
      )
      new TestJDBCMinion(config = Some(config))
      1 must beEqualTo(1)
    }

    "properly executes valid statements" in {
      val config = Map[String,Any](
        "connection" -> Map(
          "driver"  -> "org.hsqldb.jdbc.JDBCDriver",
          "dsn"     -> "jdbc:hsqldb:mem:bullfinch;shutdown=true",
          "uid"     -> "SA",
          "pwd"     -> "",
          "validation" -> "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS"
        )
      )
      val min = new TestJDBCMinion(config = Some(config))
      min.testMethod
      1 must beEqualTo(1)
    }
  }
}
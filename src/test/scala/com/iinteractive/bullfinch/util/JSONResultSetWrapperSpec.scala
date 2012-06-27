package com.iinteractive.bullfinch.util

import org.specs2.mutable._
import com.iinteractive.bullfinch.Minion

import java.sql.{Connection,DriverManager}

class JSONResultSetWrapperSpec extends Specification {

  "JSONResultSetWrapper" should {
    "properly serialize a resultset" in {

      Class.forName("org.hsqldb.jdbc.JDBCDriver")
      val conn = DriverManager.getConnection("jdbc:hsqldb:mem:testing;shutdown=true", "SA", "");
      val sMakeTable = conn.createStatement()
      sMakeTable.execute("CREATE TABLE PUBLIC.TEST_TABLE (foo INTEGER, bar VARCHAR(32))")
      
      val sAddData = conn.createStatement()
      sAddData.execute("INSERT INTO PUBLIC.TEST_TABLE (foo, bar) VALUES (1, 'Hello')")
      val sAddData2 = conn.createStatement()
      sAddData2.execute("INSERT INTO PUBLIC.TEST_TABLE (foo, bar) VALUES (2, 'World!')")
      
      val sGetData = conn.createStatement()
      val rs = sGetData.executeQuery("SELECT foo,bar FROM PUBLIC.TEST_TABLE")
      
      val wrapper = new JSONResultSetWrapper(resultSet = rs)
      wrapper.hasNext must beEqualTo(true)
      wrapper.hasNext must beEqualTo(true) // Again, as it shouldn't matter
      
      val s = wrapper.next
      s must beEqualTo("""["row_num":1,"FOO":1,"BAR":"Hello"]""")
      
      wrapper.hasNext must beEqualTo(true)
      wrapper.hasNext must beEqualTo(true) // Shouldn't matter, must be checked
      
      val s2 = wrapper.next
      s2 must beEqualTo("""["row_num":2,"FOO":2,"BAR":"World!"]""")
      
      wrapper.hasNext must beEqualTo(false)
      
      1 must beEqualTo(1)
    }
  }
}
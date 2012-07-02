package com.iinteractive.bullfinch.minion

import org.specs2.mock._
import org.specs2.mutable._
import net.spy.memcached.MemcachedClient
import net.spy.memcached.internal.OperationFuture
import scala.collection.mutable.ListBuffer

class JDBCTableScannerSpec extends Specification with Mockito {

  val validConfig = Map[String,Any](
    "connection" -> Map(
      "driver"  -> "org.hsqldb.jdbc.JDBCDriver",
      "dsn"     -> "jdbc:hsqldb:mem:bfinch2;shutdown=true",
      "uid"     -> "SA",
      "pwd"     -> "",
      "validation" -> "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS"
    ),
    "interval"    -> 1000,
    "select_query"-> "SELECT * FROM PUBLIC.TEST_TABLE2 WHERE scanned=0 ORDER BY a_float ASC LIMIT 1",
    "mark_query"  -> "UPDATE PUBLIC.TEST_TABLE2 SET scanned=1 WHERE an_int=?",
    "mark_key"    -> "an_int",
    "key_position"-> 1,
    "key_type"    -> "INTEGER",
    "publish_to"  -> "foobar"
  )

  sequential

  "The Table scanner" should {

    "Fail with missing items" in {
      new JDBCTableScanner(config = None) must throwA[Exception]
    }

    "Instantiate properly" in {

      new JDBCTableScanner(config = Some(validConfig))
      1 must beEqualTo(1)
    }

    "Select & Mark" in {
      val scanner = new JDBCTableScanner(config = Some(validConfig))
      val conn = scanner.pool.getConnection
      
      // Insert some fake data
      try {
        conn.prepareStatement("CREATE TABLE PUBLIC.TEST_TABLE2 (an_int INTEGER, a_float FLOAT, scanned INTEGER)").execute
        conn.prepareStatement("INSERT INTO PUBLIC.TEST_TABLE2 (an_int, a_float, scanned) VALUES (12, 3.14, 0)").execute
        conn.prepareStatement("INSERT INTO PUBLIC.TEST_TABLE2 (an_int, a_float, scanned) VALUES (13, 2.14, 0)").execute
      } finally {
        if(conn != null) {
          conn.close
        }
      }

      val mockClient: MemcachedClient = mock[MemcachedClient]
      val mockFuture: OperationFuture[java.lang.Boolean] = mock[OperationFuture[java.lang.Boolean]]
      var messages = new ListBuffer[String]()
      mockClient.set(anyString, anyInt, anyString) answers { (params, mock) =>
        val list = params.asInstanceOf[Array[Object]]
        messages += list(2).asInstanceOf[String]
        mockFuture
      }    
      scanner.protoClient = Some(mockClient)

      scanner.sendRows

      messages.size must beEqualTo(1)
      messages(0) must beEqualTo("""{"row_num":1,"AN_INT":13,"A_FLOAT":2.14,"SCANNED":0}""")
      messages.clear

      scanner.sendRows
      messages.size must beEqualTo(1)
      messages(0) must beEqualTo("""{"row_num":1,"AN_INT":12,"A_FLOAT":3.14,"SCANNED":0}""")

      scanner.cancel
      1 must beEqualTo(1)
    }
  }
}
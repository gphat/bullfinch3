package com.iinteractive.bullfinch.minion

import java.util.concurrent.Future
import org.specs2.mock._
import org.specs2.mutable._
import net.spy.memcached.MemcachedClient
import net.spy.memcached.internal.OperationFuture
import scala.collection.mutable.ListBuffer

class JDBCQueryRunnerSpec extends Specification with Mockito {

  val validConfig = Map[String,Any](
    "connection" -> Map(
      "driver"  -> "org.hsqldb.jdbc.JDBCDriver",
      "dsn"     -> "jdbc:hsqldb:mem:bullfinch;shutdown=true",
      "uid"     -> "SA",
      "pwd"     -> "",
      "validation" -> "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS"
    ),
    "statements" -> Map(
      "selekta" -> Map(
        "sql" -> "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS"
      ),
      "broken" -> Map(
        "sql" -> "ASDASDASDSD"
      )
    )
  )

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

    "doesn't answer busted JSON" in {

      val mockClient: MemcachedClient = mock[MemcachedClient]
      // mockClient.get(anyString) answers { queue => Some("""{}"""); None }
      
      val queryRunner = new JDBCQueryRunner(config = Some(validConfig))
      queryRunner.protoClient = Some(mockClient)

      queryRunner.handle("""trash!""")
      there were no(mockClient).set(anyString, anyInt, anyString)
    }

    "reports busted statement" in {

      val mockClient: MemcachedClient = mock[MemcachedClient]
      val mockFuture: OperationFuture[java.lang.Boolean] = mock[OperationFuture[java.lang.Boolean]]
      mockClient.set(anyString, anyInt, anyString) answers { (params, mock) =>
        val list = params.asInstanceOf[Array[Object]]
        list(2).asInstanceOf[String] must beEqualTo("""{ "ERROR":"Invalid queries: foobar" }""")
        mockFuture
      }
      
      val queryRunner = new JDBCQueryRunner(config = Some(validConfig))
      queryRunner.protoClient = Some(mockClient)

      queryRunner.handle("""{"response_queue":"foobar","statement":"foobar","params":[]}""")
      there were one(mockClient).set(anyString, anyInt, anyString)
    }

    "reports busted statements" in {

      val mockClient: MemcachedClient = mock[MemcachedClient]
      val mockFuture: OperationFuture[java.lang.Boolean] = mock[OperationFuture[java.lang.Boolean]]
      mockClient.set(anyString, anyInt, anyString) answers { (params, mock) =>
        val list = params.asInstanceOf[Array[Object]]
        list(2).asInstanceOf[String] must beEqualTo("""{ "ERROR":"Invalid queries: foobar, foobaz" }""")
        mockFuture
      }

      val queryRunner = new JDBCQueryRunner(config = Some(validConfig))
      queryRunner.protoClient = Some(mockClient)

      queryRunner.handle("""{"response_queue":"foobar","statements":["foobar","foobaz"],"params":[[],[]]}""")
      there were one(mockClient).set(anyString, anyInt, anyString)
    }

    "reports busted params" in {

      val mockClient: MemcachedClient = mock[MemcachedClient]
      val mockFuture: OperationFuture[java.lang.Boolean] = mock[OperationFuture[java.lang.Boolean]]
      mockClient.set(anyString, anyInt, anyString) answers { (params, mock) =>
        val list = params.asInstanceOf[Array[Object]]
        list(2).asInstanceOf[String] must beEqualTo("""{ "ERROR":"Invalid query parameters: selekta" }""")
        mockFuture
      }

      val queryRunner = new JDBCQueryRunner(config = Some(validConfig))
      queryRunner.protoClient = Some(mockClient)

      queryRunner.handle("""{"response_queue":"foobar","statements":["selekta"],"params":[[1],[2]]}""")
      there were one(mockClient).set(anyString, anyInt, anyString)
    }

    "reports busted params" in {

      val mockClient: MemcachedClient = mock[MemcachedClient]
      val mockFuture: OperationFuture[java.lang.Boolean] = mock[OperationFuture[java.lang.Boolean]]
      mockClient.set(anyString, anyInt, anyString) answers { (params, mock) =>
        val list = params.asInstanceOf[Array[Object]]
        list(2).asInstanceOf[String] must beEqualTo("""{ "ERROR":"Invalid query parameters: selekta" }""")
        mockFuture
      }

      val queryRunner = new JDBCQueryRunner(config = Some(validConfig))
      queryRunner.protoClient = Some(mockClient)

      queryRunner.handle("""{"response_queue":"foobar","statements":["selekta"],"params":[[1],[2]]}""")
      there were one(mockClient).set(anyString, anyInt, anyString)
    }

    "works with normal queries" in {

      val mockClient: MemcachedClient = mock[MemcachedClient]
      val mockFuture: OperationFuture[java.lang.Boolean] = mock[OperationFuture[java.lang.Boolean]]
      var messages = new ListBuffer[String]()
      mockClient.set(anyString, anyInt, anyString) answers { (params, mock) =>
        val list = params.asInstanceOf[Array[Object]]
        messages += list(2).asInstanceOf[String]
        mockFuture
      }

      val queryRunner = new JDBCQueryRunner(config = Some(validConfig))
      queryRunner.protoClient = Some(mockClient)

      queryRunner.handle("""{"response_queue":"foobar","statements":["broken"],"params":[[]]}""")
      // Put the respone in here, we'll only get an EOF as there's no resultset for an insert
      messages.size must beEqualTo(2)
      messages(0) must beEqualTo("""{ "ERROR":"Error executing SQL: Borrow prepareStatement from pool failed" }""")
      messages(1) must beEqualTo("""{ "EOF":"EOF" }""")
      messages.clear

      queryRunner.handle("""{"statements":["creata"],"params":[]}""")
      // We won't get anything back from this because we did not have a response queue
      messages.size must beEqualTo(0)
      messages.clear

      queryRunner.handle("""{"response_queue":"foobar","statements":["selekta"],"params":[]}""")
      // We won't get anything back from this because we did not have a response queue
      messages.size must beEqualTo(1)
      messages(0) must beEqualTo("""{ "EOF":"EOF" }""")
      messages.clear
      
      1 must beEqualTo(1)
    }
  }
}
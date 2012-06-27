package com.iinteractive.bullfinch.minion

import org.specs2.mock._
import org.specs2.mutable._
import com.iinteractive.bullfinch.Minion
import java.util.concurrent.TimeUnit

import net.spy.memcached.MemcachedClient

class TestKestrelMinion(config: Option[Map[String,Any]]) extends Minion(config) with KestrelBased {
  def run {
    
  }
}

class KestrelBasedSpec extends Specification with Mockito {

  "KestrelBased" should {
    "succeed with no config" in {
      val mockClient: MemcachedClient = mock[MemcachedClient]

      val kes = new TestKestrelMinion(config = None)
      1 must beEqualTo(1)
    }
  }
  
  "properly calls Memcached client" in {
    val mockClient: MemcachedClient = mock[MemcachedClient]

    val kes = new TestKestrelMinion(config = None)
    kes.protoClient = Some(mockClient)

    kes.sendMessage(queue = "someQueue", message = "Message")
    there was one(mockClient).set("someQueue", 0, "Message")
    
    kes.getMessage(queue = "someQueue", tout = 1234)
    kes.confirm(queue = "someQueue")
    there was one(mockClient).get("someQueue/open/t=1234") then one(mockClient).get("someQueue/close")
    
    kes.cancel
    there was one(mockClient).shutdown(1, TimeUnit.SECONDS)
  }
}
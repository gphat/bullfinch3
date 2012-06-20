package com.iinteractive.bullfinch.minion

import com.iinteractive.bullfinch.Minion
import java.util.LinkedHashMap
import org.apache.commons.dbcp.BasicDataSource
import scala.collection.JavaConversions._

trait JDBCBased extends Minion {
  
  val connConfig = config.get("connection").asInstanceOf[LinkedHashMap[String,Object]].toMap
  val driver = connConfig.get("driver") match {
    case Some(d) => d.asInstanceOf[String]
    case None => throw new RuntimeException("JDBC Based workers require a driver!")
  }
  val dsn = connConfig.get("dsn") match {
    case Some(d) => d.asInstanceOf[String]
    case None => throw new RuntimeException("JDBC Based workers require a dsn!")
  }
  val username = connConfig.get("uid") match {
    case Some(d) => d.asInstanceOf[String]
    case None => throw new RuntimeException("JDBC Based workers require a uid!")
  }
  val password = connConfig.get("pwd") match {
    case Some(d) => d.asInstanceOf[String]
    case None => null
  }
  val validationQuery = connConfig.get("validation") match {
    case Some(d) => d.asInstanceOf[String]
    case None => throw new RuntimeException("JDBC Based workers require a validation!")
  }
  
  lazy val pool = {
    val ds = new BasicDataSource()
		ds.setMaxActive(1)
		ds.setMaxIdle(1)
		ds.setPoolPreparedStatements(true)
		ds.setTestOnBorrow(true)
		ds.setTestWhileIdle(true)
		ds.setValidationQuery(validationQuery)

		ds.setDriverClassName(driver)
		ds.setUsername(username);
		ds.setPassword(password);
		ds.setUrl(dsn);
		ds
  }
  
  override def cancel {
    super.cancel

    log.debug("Shutting down connection pool")
    pool.close
  }
  
  override def configure {
    super.configure
    log.debug("Configure in JDBC")
  }
}
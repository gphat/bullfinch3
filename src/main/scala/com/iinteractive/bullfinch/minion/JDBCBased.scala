package com.iinteractive.bullfinch.minion

import com.iinteractive.bullfinch.Minion
import java.sql.{Connection,PreparedStatement,ResultSet}
import org.apache.commons.dbcp.BasicDataSource
import scala.collection.JavaConversions._

/**
 * A trait that wraps the boring details of creating a connection pool to a
 * database, managing Connections and PreparedStatements.
 */
trait JDBCBased extends Minion {

  val ourconfig = config match {
    case Some(c) => c
    case None => throw new RuntimeException("JDBC Based workers require a configuration")
  }
  
  val connConfig: Map[String,Any] = ourconfig.get("connection") match {
    case Some(c) => c.asInstanceOf[Map[String,Any]]
    case None => throw new RuntimeException("JDBC Based workers require a driver!")
  }
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
    case None => ""
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
  
  /**
   * Wrapper function for functions that would like a database connection.
   * Automatically closes the connection.  Does not catch exceptions or handle
   * rollbacks!
   */
  def withConnection(useTransaction: Option[Boolean] = Some(false))(body: (Connection) => Unit) {

    val useTxn = useTransaction match {
      case Some(switch) => switch
      case None         => false
    }

    val conn = pool.getConnection
    try {
      if(useTxn) {
        conn.setAutoCommit(false)
      }
      body(conn)
      // Verify this isn't eating all exceptions
      if(useTxn) {
        conn.commit
      }
    } finally {
      if(conn != null) {
        try { conn.setAutoCommit(false); conn.close } catch {
          case ex: Exception => log.error("Error releasing database connection: ", ex)
        }
      }
    }
  }
  
  /**
   * Wrapper function for functions that would like a PreparedStatement.
   * Automatically closes the statement. Does not catch exceptions.
   */
  def withStatement(conn: Connection, sql: String)(body: (PreparedStatement) => Unit) {
    
    val statement = conn.prepareStatement(sql)
    try {
      body(statement)
    } finally {
      if(statement != null) {
        try { statement.close } catch {
          case ex: Exception => log.error("Error preparing statement: ", ex)
        }
      }
    }
  }
  
  /**
   * Cancel implementation from Minion. Closes the connection pool.
   */
  override def cancel {
    super.cancel

    log.debug("Shutting down connection pool")
    pool.close
  }
}
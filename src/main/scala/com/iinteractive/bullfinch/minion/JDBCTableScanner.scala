package com.iinteractive.bullfinch.minion

import com.iinteractive.bullfinch.Minion
import com.iinteractive.bullfinch.util.JSONResultSetWrapper

class JDBCTableScanner(config: Option[Map[String,Any]]) extends Minion(config) with KestrelBased with JDBCBased {
  
  val interval = config.get("interval").asInstanceOf[Integer]
  val selectQuery = config.get("select_query").asInstanceOf[String]
  val publishTo = config.get("publish_to").asInstanceOf[String]
  val markQuery = config.get("mark_query").asInstanceOf[String]
  val markKey = config.get("mark_key").asInstanceOf[String]
  val keyPosition = config.get("key_position").asInstanceOf[Integer]
  val keyType = config.get("key_type").asInstanceOf[String]

  def run = {

    while(this.shouldContinue) {
      
      Thread.sleep(interval.toLong)
      sendRows
    }
  }

  def sendRows {

    withConnection(Some(false)) { conn =>

      withStatement(conn, selectQuery) { selectPrep =>
        val rs = selectPrep.executeQuery
        try {
          val wrapper = new JSONResultSetWrapper(resultSet = rs)
          while(wrapper.hasNext) {
            sendMessage(publishTo, wrapper.next)
            log.debug("Marking sent row")
            withStatement(conn, markQuery) { markPrep =>

              keyType match {
                case "BOOLEAN"  => markPrep.setBoolean(keyPosition.intValue, rs.getBoolean(markKey))
                case "NUMBER"   => markPrep.setDouble(keyPosition.intValue, rs.getDouble(markKey))
                case "INTEGER"  => markPrep.setInt(keyPosition.intValue, rs.getInt(markKey))
                case "STRING"   => markPrep.setString(keyPosition.intValue, rs.getString(markKey))
                case _          => {
                  throw new IllegalArgumentException("Don't understand key_type for marking query")
                }
              }
              markPrep.execute
            }
          }
        } catch {
          case ex: Exception => {
            ex.printStackTrace
            log.error("Failed table scanning", ex)
            if(rs != null) {
              rs.close
            }
          }
        }
      }
    }
  }
}
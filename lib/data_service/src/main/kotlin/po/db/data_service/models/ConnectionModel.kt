package po.db.data_service.models

import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import po.db.data_service.structure.ConnectionContext


class ConnectionInfo(val host: String, val dbName: String, val user: String, val pwd: String, val port: String = "3306") {

   val connections : MutableList<ConnectionContext> = mutableListOf()

   inner class ConnectorInfo(
       val driverClassName : String = "com.mysql.cj.jdbc.Driver",
       val jdbcUrl : String = "jdbc:mysql://$host:$port/$dbName?user=$user&password=$pwd",
       val maximumPoolSize : Int = 10,
       val isAutoCommit: Boolean = false,
       val transactionIsolation: String  = "TRANSACTION_REPEATABLE_READ"
    )


    var lastError : String? = null

    var connection: Database? = null

   var hikariDataSource : HikariDataSource? = null

   val driverClassName = "com.mysql.cj.jdbc.Driver"

   val connectionInfo = ConnectorInfo(driverClassName, "jdbc:mysql://$host:$port/$dbName?user=$user&password=$pwd", 10, false, "TRANSACTION_REPEATABLE_READ")

    fun getConnectionString(): String{
        return  "jdbc:mysql://$host:$port/$dbName?user=$user&password=$pwd"
    }
}
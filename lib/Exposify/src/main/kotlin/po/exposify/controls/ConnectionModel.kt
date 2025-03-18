package po.exposify.controls

import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import po.exposify.scope.connection.ConnectionContext


class ConnectionInfo(
    val host: String,
    val dbName: String,
    val user: String,
    val pwd: String,
    val port: String = "5432",
    val driver: String = "org.postgresql.Driver"
){

   val connections : MutableList<ConnectionContext> = mutableListOf()

   inner class ConnectorInfo(
       val driverClassName : String = "org.postgresql.Driver",
       val jdbcUrl : String = "jdbc:postgresql://$host:$port/$dbName?user=$user&password=$pwd",
       val maximumPoolSize : Int = 10,
       val isAutoCommit: Boolean = false,
       val transactionIsolation: String  = "TRANSACTION_REPEATABLE_READ"
    )

    fun  setError(ex: Exception){

    }

    var lastError : String? = null
    var connection: Database? = null
    var hikariDataSource : HikariDataSource? = null
    val driverClassName = "org.postgresql.Driver"

    val connectionInfo = ConnectorInfo(driverClassName, "jdbc:postgresql://$host:$port/$dbName?user=$user&password=$pwd", 10, false, "TRANSACTION_REPEATABLE_READ")

    fun getConnectionString(): String{
        return  "jdbc:postgresql://$host:$port/$dbName?user=$user&password=$pwd"
    }
}
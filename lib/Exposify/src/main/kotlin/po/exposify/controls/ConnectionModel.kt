package po.exposify.controls

import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import po.exposify.scope.connection.ConnectionContext


class ConnectionInfo(
    val host: String = "",
    val port: String = "",
    val dbName: String,
    val user: String,
    val pwd: String,
    val driver: String = "org.postgresql.Driver",
    val jdbcUrl: String? = null
){
   val connections : MutableList<ConnectionContext> = mutableListOf()

    var lastError : String? = null
    var connection: Database? = null
    var hikariDataSource : HikariDataSource? = null
    val driverClassName = "org.postgresql.Driver"

    val errorList = mutableListOf<String>()

    fun registerError(th: Throwable){
        errorList.add(th.message.toString())
    }

    fun getConnectionString(): String{
        if(jdbcUrl != null){
            return  jdbcUrl
        }else{
            return  "jdbc:postgresql://$host:$port/$dbName"
        }
    }
}
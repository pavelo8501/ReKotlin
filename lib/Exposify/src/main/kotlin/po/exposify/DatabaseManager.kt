package po.exposify

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database


import po.exposify.controls.ConnectionInfo
import po.exposify.scope.connection.ConnectionClass
import po.exposify.scope.connection.ConnectionContext

fun launchService(connection:ConnectionContext, block: ConnectionContext.()-> Unit ){
    if(connection.isOpen){
        connection.block()
    }
}

object DatabaseManager {

    private val connections  = mutableListOf<ConnectionClass>()

    private fun addConnection(connection : ConnectionClass){
        connections.add(connection)
    }

    private fun provideDataSource(connectionInfo:ConnectionInfo): HikariDataSource {
        val hikariConfig= HikariConfig().apply {
            driverClassName = connectionInfo.driverClassName
            jdbcUrl = connectionInfo.getConnectionString()
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        return HikariDataSource(hikariConfig)
    }

    fun openConnection(
        connectionInfo : ConnectionInfo,
        context: ConnectionContext.()->Unit
    ): Boolean  {
        connectionInfo.hikariDataSource = provideDataSource(connectionInfo)
        try{
            val newConnection = Database.connect(connectionInfo.hikariDataSource!!)
            val connectionClass =  ConnectionClass(connectionInfo, newConnection)
            val connectionContext =  ConnectionContext(
                "Connection ${connectionInfo.dbName}",
                newConnection, connectionClass).also {
                    connectionInfo.connections.add(it)
            }
            addConnection(connectionClass)
            connectionContext.context()
            return true
        }catch (e: Exception){
            connectionInfo.lastError = e.message
            return false
        }
    }



}
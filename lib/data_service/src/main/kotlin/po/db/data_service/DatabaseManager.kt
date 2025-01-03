package po.db.data_service

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database


import po.db.data_service.controls.ConnectionInfo
import po.db.data_service.scope.connection.ConnectionClass
import po.db.data_service.scope.connection.ConnectionContext

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
        connection: (ConnectionContext.() -> Unit)? = null
    ): ConnectionContext {
        connectionInfo.hikariDataSource = provideDataSource(connectionInfo)
        try{
           val newConnection = Database.connect(connectionInfo.hikariDataSource!!)
            val connectionClass =  ConnectionClass(connectionInfo)
            val connectionContext =  ConnectionContext("Connection ${connectionInfo.dbName}",newConnection, connectionClass).also {
                connectionInfo.connections.add(it)
            }
            connection?.invoke(connectionContext)
            addConnection(connectionClass)
            return connectionContext
        }catch (e: Exception){
            connectionInfo.lastError = e.message
            throw e
        }
    }
}
package po.db.data_service

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import po.db.data_service.models.ConnectionInfo
import po.db.data_service.structure.ConnectionContext

object DatabaseManager {

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
           val databaseContext =  ConnectionContext("Connection ${connectionInfo.dbName}",newConnection).also {
                connectionInfo.connections.add(it)
            }
            connection?.invoke(databaseContext)
            return databaseContext
        }catch (e: Exception){
            connectionInfo.lastError = e.message
            throw e
        }
    }
}
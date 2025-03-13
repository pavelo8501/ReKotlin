package po.exposify

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.delay
import org.jetbrains.exposed.sql.Database


import po.exposify.controls.ConnectionInfo
import po.exposify.scope.connection.ConnectionClass
import po.exposify.scope.connection.ConnectionContext
import po.exposify.scope.connection.models.ConnectionSettings

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

    private var  connectionUpdated : ((String, Boolean)-> Unit)? = null
    fun onConnectionUpdate(fn: (String, Boolean)-> Unit) {
        connectionUpdated = fn
    }

    private fun provideDataSource(connectionInfo:ConnectionInfo): HikariDataSource {
        val hikariConfig= HikariConfig().apply {
            driverClassName = connectionInfo.driverClassName
            jdbcUrl = connectionInfo.getConnectionString()
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_READ_COMMITTED" // Changed from TRANSACTION_REPEATABLE_READ
            validate()
        }
        return HikariDataSource(hikariConfig)
    }

    suspend fun openConnection(
        connectionInfo : ConnectionInfo,
        settings : ConnectionSettings = ConnectionSettings(5),
        context: ConnectionContext.()->Unit
    ): Boolean {
        var retriesLeft = settings.retries.toInt()
        while (retriesLeft != 0) {
            runCatching {
                connectionInfo.hikariDataSource = provideDataSource(connectionInfo)
                val newConnection = Database.connect(connectionInfo.hikariDataSource!!)
                val connectionClass = ConnectionClass(connectionInfo, newConnection)
                val connectionContext = ConnectionContext(
                    "Connection ${connectionInfo.dbName}",
                    newConnection, connectionClass
                ).also {
                    connectionInfo.connections.add(it)
                }
                addConnection(connectionClass)
                connectionContext.context()
                connectionUpdated?.invoke("Connected", true)
                return true
            }.getOrElse {
                connectionUpdated?.invoke(it.message.toString(), false)
                connectionInfo.setError(it as Exception)
                connectionInfo.lastError = it.message
                if (retriesLeft > 0) {
                    retriesLeft--
                    delay(10000)
                }
            }
        }
        return false
    }
}
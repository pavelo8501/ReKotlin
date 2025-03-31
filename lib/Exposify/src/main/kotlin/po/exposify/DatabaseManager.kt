package po.exposify

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.delay
import org.jetbrains.exposed.sql.Database
import po.auth.sessions.interfaces.ManagedSession
import po.exposify.controls.ConnectionInfo
import po.exposify.scope.connection.ConnectionClass
import po.exposify.scope.connection.ConnectionClass2
import po.exposify.scope.connection.ConnectionContext
import po.exposify.scope.connection.ConnectionContext2
import po.exposify.scope.connection.models.ConnectionSettings

fun echo(ex: Exception, message: String? = null){
    println("Exception happened in Exposify:  Exception:${ex.message.toString()}. $message")
}

fun launchService(connection:ConnectionContext, block: ConnectionContext.()-> Unit ){
    if(connection.isOpen){
        connection.block()
    }
}

object DatabaseManager {

    private val connections  = mutableListOf<ConnectionClass2>()

    private fun addConnection(connection : ConnectionClass2){
        connections.add(connection)
    }

    private var  connectionUpdated : ((String, Boolean)-> Unit)? = null
    fun onConnectionUpdate(fn: (String, Boolean)-> Unit) {
        connectionUpdated = fn
    }

    private fun provideDataSource(connectionInfo:ConnectionInfo): HikariDataSource? {
        try {
            val hikariConfig= HikariConfig().apply {
                driverClassName = connectionInfo.driverClassName
                jdbcUrl = connectionInfo.getConnectionString()
                username = connectionInfo.user
                password = connectionInfo.pwd
                maximumPoolSize = 10
                isAutoCommit = false
                transactionIsolation = "TRANSACTION_READ_COMMITTED" // Changed from TRANSACTION_REPEATABLE_READ
                validate()
            }
            val source =  HikariDataSource(hikariConfig)
            return source
        }catch (ex: Exception){
            println(ex.message.toString())
            return null
        }catch (th: Throwable){
            println(th.message.toString())
            return null
        }
    }

    fun openConnectionSync(
        connectionInfo : ConnectionInfo,
        sessionManager: ManagedSession,
        context: (ConnectionContext2.()->Unit)? = null
    ): ConnectionContext2? {

        try {
            connectionInfo.hikariDataSource = provideDataSource(connectionInfo)
            val newConnection = Database.connect(connectionInfo.hikariDataSource!!)
            val connectionClass = ConnectionClass2(connectionInfo, newConnection, sessionManager)
            val connectionContext = ConnectionContext2(
                "Connection ${connectionInfo.dbName}",
                newConnection, connectionClass
            ).also {
                connectionInfo.connections.add(it)
            }
            addConnection(connectionClass)

            context?.invoke(connectionContext)
            connectionUpdated?.invoke("Connected", true)
            return connectionContext
        }catch (ex: Exception){
            println(ex.message)
            return  null
        }catch (th: Throwable){
            println(th.message.toString())
            return null
        }
    }

    suspend fun openConnection(
        connectionInfo : ConnectionInfo,
        sessionManager: ManagedSession,
        settings : ConnectionSettings = ConnectionSettings(5),
        context: ConnectionContext2.()->Unit
    ): Boolean {
        var retriesLeft = settings.retries.toInt()
        while (retriesLeft != 0) {
            runCatching {
                connectionInfo.hikariDataSource = provideDataSource(connectionInfo)
                val newConnection = Database.connect(connectionInfo.hikariDataSource!!)
                val connectionClass = ConnectionClass2(connectionInfo, newConnection, sessionManager)
                val connectionContext = ConnectionContext2(
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
                connectionInfo.registerError(it)
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
package po.exposify

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import po.exposify.scope.connection.models.ConnectionInfo
import po.exposify.scope.connection.ConnectionClass
import po.exposify.scope.connection.ConnectionContext
import po.exposify.scope.connection.models.ConnectionSettings
import po.lognotify.classes.task.models.TaskConfig
import po.lognotify.extensions.runTask
import po.lognotify.extensions.runTaskAsync
import po.lognotify.extensions.runTaskBlocking


//fun launchService(connection: ConnectionContext, block: ConnectionContext.()-> Unit ){
//    if(connection.isOpen){
//        connection.block()
//    }
//}

object DatabaseManager {

    private val connections  = mutableListOf<ConnectionClass>()

    private fun addConnection(connection : ConnectionClass){
        connections.add(connection)
    }

    private var  connectionUpdated : ((String, Boolean)-> Unit)? = null
    fun onConnectionUpdate(fn: (String, Boolean)-> Unit) {
        connectionUpdated = fn
    }

    private fun tryConnect(connectionInfo:ConnectionInfo): ConnectionClass {
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
            val hikariDataSource =  HikariDataSource(hikariConfig)
            val newConnection = Database.connect(hikariDataSource)
            val connectionClass =  ConnectionClass(this, connectionInfo, newConnection)
            addConnection(connectionClass)
            return  ConnectionClass(this, connectionInfo, newConnection)
        }catch (th: Throwable){
            connectionUpdated?.invoke(th.message.toString(), false)
            println(th.message.toString())
            throw th
        }
    }

    fun openConnection(
        connectionInfo : ConnectionInfo,
        settings : ConnectionSettings = ConnectionSettings(5),
        context: (ConnectionContext.()->Unit)? = null
    ): ConnectionContext {
      return runTask("openConnection", TaskConfig(attempts = settings.retries, moduleName = "DatabaseManager")) {
            val connectionClass = tryConnect(connectionInfo)
            val connectionContext = connectionClass.connectionContext()
            if (context != null) {
                connectionContext.context()
            }
            connectionUpdated?.invoke("Connected", true)
            connectionContext
        }.resultOrException()
    }

    fun openConnectionAsync(
        connectionInfo : ConnectionInfo,
        settings : ConnectionSettings = ConnectionSettings(5),
        context: suspend ConnectionContext.()->Unit
    ) = runTaskBlocking("openConnectionAsync",TaskConfig(attempts =  settings.retries, moduleName = "DatabaseManager") ) {

        val connectionClass =  tryConnect(connectionInfo)
        val connectionContext =  connectionClass.connectionContext()
        connectionContext.context()
        connectionUpdated?.invoke("Connected", true)
    }
}
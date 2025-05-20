package po.exposify

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import po.exposify.extensions.getOrOperationsEx
import po.exposify.scope.connection.models.ConnectionInfo
import po.exposify.scope.connection.ConnectionClass
import po.exposify.scope.connection.models.ConnectionSettings
import po.lognotify.classes.task.models.TaskConfig
import po.lognotify.extensions.runTask
import po.lognotify.extensions.runTaskBlocking
import po.misc.serialization.SerializerInfo


object DatabaseManager {

    private var  connectionUpdated : ((String, Boolean)-> Unit)? = null
    internal val connections  = mutableListOf<ConnectionClass>()
    internal fun addConnection(connection : ConnectionClass){
        connections.add(connection)
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
            return  connectionClass
        }catch (th: Throwable){
            connectionUpdated?.invoke(th.message.toString(), false)
            println(th.message.toString())
            throw th
        }
    }

    @PublishedApi
    internal fun <T> provideSerializer(serializer: SerializerInfo<T>){
        connections.forEach {
            it.registerSerializer(serializer)
        }
    }

    fun onConnectionUpdate(fn: (String, Boolean)-> Unit) {
        connectionUpdated = fn
    }

    fun openConnection(
        connectionInfo : ConnectionInfo,
        settings : ConnectionSettings = ConnectionSettings(5),
    ): ConnectionClass {
      return runTask("openConnection", TaskConfig(attempts = settings.retries, moduleName = "DatabaseManager")) {
          val connectionClass = tryConnect(connectionInfo)
          connectionUpdated?.invoke("Connected", true)
          connectionClass
        }.resultOrException()
    }

    fun openConnectionAsync(
        connectionInfo : ConnectionInfo,
        settings : ConnectionSettings = ConnectionSettings(5),
    ):ConnectionClass {
        return runTaskBlocking("openConnectionAsync", TaskConfig(attempts = settings.retries, moduleName = "DatabaseManager")) {
            val connectionClass = tryConnect(connectionInfo)
            connectionUpdated?.invoke("Connected", true)
            connectionClass
        }.resultOrException()
    }

}

fun withConnection(
    connectionInfo: ConnectionInfo
): ConnectionClass {
    val connectionString =  connectionInfo.getConnectionString()
    val connection = DatabaseManager.connections.first { it.connectionInfo.getConnectionString() == connectionString }
    val effectiveConnection = connection.getOrOperationsEx("Connection $connectionString not found")
    return connection
}


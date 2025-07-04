package po.exposify

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import po.exposify.common.classes.DBManagerHooks
import po.exposify.extensions.getOrInit
import po.exposify.scope.connection.models.ConnectionInfo
import po.exposify.scope.connection.ConnectionClass
import po.exposify.scope.connection.models.ConnectionSettings
import po.lognotify.classes.task.models.TaskConfig
import po.lognotify.extensions.runTask
import po.lognotify.extensions.runTaskBlocking
import po.misc.exceptions.ManagedException
import po.misc.exceptions.text
import po.misc.interfaces.IdentifiableClass
import po.misc.interfaces.IdentifiableContext
import po.misc.serialization.SerializerInfo

object DatabaseManager: IdentifiableContext {

    override val contextName: String = "DatabaseManager"

    private var  connectionUpdated : ((String, Boolean)-> Unit)? = null
    internal val connections  = mutableListOf<ConnectionClass>()
    internal fun addConnection(connection : ConnectionClass){
        connections.add(connection)
    }

    private fun tryConnect(connectionInfo:ConnectionInfo, hooks:DBManagerHooks?): ConnectionClass {
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
            if(hooks != null){
                hooks.onConnectionFail?.invoke(th)
            }else{
                println(th.text())
            }
            throw th
        }
    }

    @PublishedApi
    internal fun signalCloseConnection(issuer: IdentifiableClass, connectionClass: ConnectionClass){
        println("Close connection signal received from ${issuer.completeName}")
        connectionClass.close()
    }

    @PublishedApi
    internal fun <T> provideSerializer(serializer: SerializerInfo<T>){
        connections.forEach {
            it.registerSerializer(serializer)
        }
    }

    fun openConnection(
        connectionInfo : ConnectionInfo?,
        settings : ConnectionSettings = ConnectionSettings(5),
        hooks: DBManagerHooks? = null
    ): ConnectionClass {
      return runTask("openConnection", TaskConfig(attempts = settings.retries, moduleName = "DatabaseManager")) {
         val effectiveConnectionInfo = connectionInfo?:hooks?.onBeforeConnection?.invoke()
         val connection = if(effectiveConnectionInfo == null){
             connections.firstOrNull().getOrInit("No connection info was provided nor opened connections exist")
          }else{
             val existentConnection =  connections.firstOrNull { it.connectionInfo.key == effectiveConnectionInfo.key }
               existentConnection?.let {
                  hooks?.onExistentConnection?.invoke(it)
                  it
              } ?: run {
                  val newConnection = tryConnect(effectiveConnectionInfo, hooks)
                  hooks?.onNewConnection?.invoke(newConnection)
                  connectionUpdated?.invoke("Connected", true)
                 newConnection
              }
          }
          connection
        }.resultOrException()
    }

    fun openConnectionAsync(
        connectionInfo : ConnectionInfo?,
        settings : ConnectionSettings = ConnectionSettings(5),
        hooks: DBManagerHooks? = null
    ):ConnectionClass {
        return runTaskBlocking("openConnectionAsync", TaskConfig(attempts = settings.retries, moduleName = "DatabaseManager")) {
            val effectiveConnectionInfo = connectionInfo?:hooks?.onBeforeConnection?.invoke()
            val connection = if(effectiveConnectionInfo == null){
                connections.firstOrNull().getOrInit("No connection info was provided nor opened connections exist")
            }else{
                val existentConnection =  connections.firstOrNull { it.connectionInfo.key == effectiveConnectionInfo.key }
                existentConnection?.let {
                    hooks?.onExistentConnection?.invoke(it)
                    it
                } ?: run {
                    val newConnection = tryConnect(effectiveConnectionInfo, hooks)
                    hooks?.onNewConnection?.invoke(newConnection)
                    connectionUpdated?.invoke("Connected", true)
                    newConnection
                }
            }
            connection
        }.resultOrException()
    }

    fun closeAllConnections() {
        synchronized(connections) {
            connections.forEach {
                try {
                    if (it.isConnectionOpen) {
                        it.close()
                        println("Closed DB connection: $it")
                    }
                } catch (e: Exception) {
                    println("Error while closing connection: ${e.message}")
                }
            }
            connections.clear()
        }
    }
}


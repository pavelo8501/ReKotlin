package po.exposify

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import po.exposify.common.classes.DBManagerHooks
import po.exposify.dto.helpers.warning
import po.exposify.extensions.getOrInit
import po.exposify.scope.connection.ConnectionClass
import po.exposify.scope.connection.models.ConnectionInfo
import po.exposify.scope.connection.models.ConnectionSettings
import po.lognotify.TasksManaged
import po.lognotify.common.configuration.TaskConfig
import po.lognotify.extensions.runTask
import po.lognotify.extensions.runTaskBlocking
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.data.processors.SeverityLevel
import po.misc.exceptions.throwableToText
import po.misc.serialization.SerializerInfo

object DatabaseManager : TasksManaged {
    override val contextName: String = "DatabaseManager"

    override val identity: CTXIdentity<out CTX> = asIdentity()

    private var connectionUpdated: ((String, Boolean) -> Unit)? = null
    internal val connections = mutableListOf<ConnectionClass>()

    internal fun addConnection(connection: ConnectionClass) {
        connections.add(connection)
    }

    private fun tryConnect(
        connectionInfo: ConnectionInfo,
        hooks: DBManagerHooks?,
    ): ConnectionClass {
        try {
            val hikariConfig =
                HikariConfig().apply {
                    driverClassName = connectionInfo.driverClassName
                    jdbcUrl = connectionInfo.getConnectionString()
                    username = connectionInfo.user
                    password = connectionInfo.pwd
                    maximumPoolSize = 10
                    isAutoCommit = false
                    transactionIsolation = "TRANSACTION_READ_COMMITTED" // Changed from TRANSACTION_REPEATABLE_READ
                    validate()
                }
            val hikariDataSource = HikariDataSource(hikariConfig)
            val newConnection = Database.connect(hikariDataSource)
            val connectionClass = ConnectionClass(this, connectionInfo, newConnection)
            addConnection(connectionClass)
            return connectionClass
        } catch (th: Throwable) {
            connectionUpdated?.invoke(th.message.toString(), false)
            if (hooks != null) {
                hooks.onConnectionFail?.invoke(th)
            } else {
                println(th.throwableToText())
            }
            throw th
        }
    }

    @PublishedApi
    internal fun signalCloseConnection(
        issuer: CTX,
        connectionClass: ConnectionClass,
    ) {
        warning("Close connection signal received from ${issuer.completeName}")
        connectionClass.close()
        connections.remove(connectionClass)
    }

    @PublishedApi
    internal fun <T> provideSerializer(serializer: SerializerInfo<T>) {
        connections.forEach {
            it.registerSerializer(serializer)
        }
    }

    fun openConnection(
        connectionInfo: ConnectionInfo?,
        settings: ConnectionSettings = ConnectionSettings(5),
        hooks: DBManagerHooks? = null,
    ): ConnectionClass =
        runTask("openConnection", TaskConfig(attempts = settings.retries)) {
            val effectiveConnectionInfo = connectionInfo ?: hooks?.onBeforeConnection?.invoke()
            val connection =
                if (effectiveConnectionInfo == null) {
                    connections.firstOrNull().getOrInit(this)
                } else {
                    val existentConnection = connections.firstOrNull { it.connectionInfo.key == effectiveConnectionInfo.key }
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

    fun openConnectionAsync(
        connectionInfo: ConnectionInfo?,
        settings: ConnectionSettings = ConnectionSettings(5),
        hooks: DBManagerHooks? = null,
    ): ConnectionClass =
        runTaskBlocking("openConnectionAsync", TaskConfig(attempts = settings.retries)) {
            val effectiveConnectionInfo = connectionInfo ?: hooks?.onBeforeConnection?.invoke()
            val connection =
                if (effectiveConnectionInfo == null) {
                    connections.firstOrNull().getOrInit(this)
                } else {
                    val existentConnection = connections.firstOrNull { it.connectionInfo.key == effectiveConnectionInfo.key }
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

package po.exposify

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import po.exposify.common.classes.DBManagerHooks
import po.exposify.dto.helpers.warning
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.getOrInit
import po.exposify.scope.connection.ConnectionClass
import po.exposify.scope.connection.models.ConnectionConfig
import po.exposify.scope.connection.models.ConnectionInfo
import po.exposify.scope.connection.models.ConnectionSettings
import po.exposify.scope.service.ServiceClass
import po.lognotify.TasksManaged
import po.lognotify.common.configuration.TaskConfig
import po.lognotify.launchers.runTask
import po.lognotify.launchers.runTaskBlocking
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.Identifiable
import po.misc.context.asIdentity
import po.misc.exceptions.throwableToText
import po.misc.serialization.SerializerInfo
import po.misc.types.safeCast
import kotlin.reflect.KClass

object DatabaseManager : TasksManaged {
    override val contextName: String = "DatabaseManager"

    override val identity: CTXIdentity<out CTX> = asIdentity()

    private var connectionUpdated: ((String, Boolean) -> Unit)? = null
    internal val connections = mutableListOf<ConnectionClass>()

    internal fun addConnection(connection: ConnectionClass) {
        connections.add(connection)
    }


    private fun <DTO: ModelDTO> serviceByDataClass(dataModelClass : KClass<*>, connectionClass: ConnectionClass): ServiceClass<DTO, *, *>?{
        val serviceKey =  connectionClass.services.keys.firstOrNull { it.dataType.kClass == dataModelClass }
        if(serviceKey != null){
          return connectionClass.services[serviceKey]?.safeCast<ServiceClass<DTO, *, *>>()
        }
        return null
    }

    fun <D, DTO: ModelDTO> notifyOnUpdated(
        identity: CTXIdentity<D>,
        connectionClass: ConnectionClass,
        callback: (DTO)-> Unit
    ) where D: DataModel, D: Identifiable<D>{
        serviceByDataClass<DTO>(identity.typeData.kClass, connectionClass)?.let {service->
            service.serviceContext.setTracking(identity as CTXIdentity<*>, callback)
        }
    }

    private fun tryConnect(connectionInfo: ConnectionInfo, hooks: DBManagerHooks?): ConnectionClass {
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
            val connectionClass = ConnectionClass(this, newConnection)
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

    private fun tryConnect(
        connectionConfig: ConnectionConfig,
        hooks: DBManagerHooks?,
    ): ConnectionClass {
        try {
            val newConnection = Database.connect(connectionConfig.hikariDataSource)
            val connectionClass = ConnectionClass(this, newConnection)
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
        connectionConfig: ConnectionConfig,
        settings: ConnectionSettings = ConnectionSettings(5),
        hooks: DBManagerHooks? = null,
    ): ConnectionClass =
        runTask("openConnection", TaskConfig(attempts = settings.retries)) {
            notify("Trying to connect...")
            hooks?.onBeforeConnection?.invoke()
            val newConnection = tryConnect(connectionConfig, hooks)
            hooks?.onNewConnection?.invoke(newConnection)
            connectionUpdated?.invoke("Connected", true)
            notify("Connection open")
            newConnection
        }.resultOrException()

    fun openConnection(
        connectionInfo: ConnectionInfo?,
        settings: ConnectionSettings = ConnectionSettings(5),
        hookBuilder: DBManagerHooks.()-> Unit
    ): ConnectionClass{
        val hooks = DBManagerHooks()
        hooks.hookBuilder()
        return openConnection(connectionInfo, settings, hooks)
    }

    fun openConnection(
        connectionInfo: ConnectionInfo?,
        settings: ConnectionSettings = ConnectionSettings(5),
        hooks: DBManagerHooks? = null,
    ): ConnectionClass =
        runTask("openConnection", TaskConfig(attempts = settings.retries)) {
            notify("Trying to connect...")
            val effectiveConnectionInfo = connectionInfo ?: hooks?.onBeforeConnection?.invoke()
            val connection =
                if (effectiveConnectionInfo == null) {
                    connections.firstOrNull().getOrInit(this)
                } else {
                    val newConnection = tryConnect(effectiveConnectionInfo, hooks)
                    hooks?.onNewConnection?.invoke(newConnection)
                    connectionUpdated?.invoke("Connected", true)
                    newConnection
                }
            notify("Connection open")
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
                    val newConnection = tryConnect(effectiveConnectionInfo, hooks)
                    hooks?.onNewConnection?.invoke(newConnection)
                    connectionUpdated?.invoke("Connected", true)
                    newConnection
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

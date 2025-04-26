package po.restwraptor.interfaces

import io.ktor.network.sockets.Connection
import io.ktor.server.engine.ConnectorType
import io.ktor.server.engine.EngineConnectorConfig
import po.restwraptor.models.configuration.ApiConfig
import po.restwraptor.models.server.WraptorRoute

interface WraptorHandler {
    fun stop(gracePeriod: Long = 5000)
    fun getRoutes(callback : ((List<WraptorRoute>)-> Unit)? = null):List<WraptorRoute>
    fun getConnectors(callback : (List<EngineConnectorConfig>)-> Unit )
    fun getConfig(): ApiConfig

}
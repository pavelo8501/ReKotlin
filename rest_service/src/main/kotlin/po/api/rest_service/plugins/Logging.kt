package po.api.rest_service.plugins

import io.ktor.server.application.createApplicationPlugin
import po.api.rest_service.logger.LoggingService
import po.api.rest_service.server.ApiServer

val LoggingPlugin = createApplicationPlugin(name = "LoggingPlugin") {
    val apiLogger = LoggingService()

    onCall { call ->
        // On Call Logic
    }

    onCallReceive { call, body ->
        // Before receiving data from the client logic
    }

    onCallRespond { call, body ->
       //Some Logic after sending data
    }
    application.attributes.put(ApiServer.loggerKey, apiLogger)
    println("Logger registered in Application: ${application.hashCode()}")
}
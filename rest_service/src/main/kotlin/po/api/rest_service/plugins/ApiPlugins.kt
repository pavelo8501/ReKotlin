package po.api.rest_service.plugins

import po.api.rest_service.logger.LoggingService

class ApiPlugins(private val apiLogger: LoggingService) {

//    companion object {
//        val Plugin = createApplicationPlugin(name = "LoggingPlugin", createConfiguration = ::LoggingService) {
//            val apiLogger = pluginConfig
//            onCall { call ->
//            }
//            application.attributes.put(ApiServer.loggerKey, apiLogger)
//            println("Logger registered in Application: ${application.hashCode()}")
//        }
//    }

}
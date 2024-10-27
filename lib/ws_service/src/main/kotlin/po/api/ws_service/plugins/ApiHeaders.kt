package po.api.ws_service.plugins

import io.ktor.server.application.createApplicationPlugin


class Configuration {
    var headerName = "X-Api-Header"
    var headerValue = "Some Value"
}


val ApiHeaderPlugin = createApplicationPlugin(
    name = "ApiHeaderPlugin",
    createConfiguration = ::Configuration
) {

    val headerName = pluginConfig.headerName
    val headerValue = pluginConfig.headerValue

    pluginConfig.apply {
        onCall { call ->
            call.response.headers.append(headerName, headerValue)
        }
    }

    onCall { call ->
        call.response.headers.append(headerName,headerValue)
    }
}

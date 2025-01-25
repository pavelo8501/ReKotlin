package po.wswraptor.plugins

import io.ktor.server.application.createApplicationPlugin


val HeadersPlugin = createApplicationPlugin(
    name = "ApiHeaderPlugin",
    createConfiguration = ::HeadersPluginConfiguration
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

class HeadersPluginConfiguration {
    var headerName = "X-Api-Header"
    var headerValue = "Some Value"
}

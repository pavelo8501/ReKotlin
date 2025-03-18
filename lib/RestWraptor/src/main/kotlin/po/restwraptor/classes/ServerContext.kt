package po.restwraptor.classes

import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.engine.EmbeddedServer
import po.restwraptor.models.configuration.ApiConfig
import po.restwraptor.models.configuration.AuthenticationConfig
import po.restwraptor.models.configuration.WraptorConfig

class ServerContext (
    val apiConfig : ApiConfig,
    val authConfig  : AuthenticationConfig,
    val wraptorConfig: WraptorConfig,
    val coreContext: CoreContext,
    val embeddedServer: EmbeddedServer<*,*>,
    val application : Application
) {



}
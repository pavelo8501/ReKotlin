package po.wswraptor.classes

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.pluginOrNull
import io.ktor.server.application.pluginRegistry
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.Routing
import io.ktor.server.routing.routing
import po.lognotify.eventhandler.RootEventHandler
import po.lognotify.eventhandler.interfaces.CanNotify
import po.restwraptor.classes.ConfigContext
import po.restwraptor.classes.ConfigContextInterface
import po.restwraptor.models.configuration.ApiConfig
import po.wswraptor.components.serializationfactory.SerializationFactory
import po.wswraptor.convertors.JsonConvertor
import po.wswraptor.convertors.JsonConvertorConfig
import po.wswraptor.models.configuration.WsApiConfig
import po.wswraptor.models.request.WSRequest
import po.wswraptor.models.response.WSResponse
import po.wswraptor.plugins.HeadersPlugin
import po.wswraptor.webSocket

class WSConfigContext(
    val app: Application,
    val wsConfig: WsApiConfig? = null,
    apiConfigContext: ConfigContext = ConfigContext(app)
): ConfigContextInterface by apiConfigContext, CanNotify{

    override val eventHandler = RootEventHandler("WSConfigContext")

    val wsApiConfig  = wsConfig?: WsApiConfig()

    private var contentConfigurationFn : (()-> Unit)? = null

    private fun configRouting(block: Routing.()-> Unit){
        app.apply {
            routing {
                this.block()
            }
        }
    }

    private fun configHeadersPlugin(){
        app.apply {
            if (pluginOrNull(HeadersPlugin) != null) {
                println("HeadersPlugin installation skipped. Custom ContentNegotiation already installed")
            }else{
                info("Installing Default HeadersPlugin")
                install(HeadersPlugin)
            }
        }
    }

    private fun configContentNegotiations(){
        app.apply {
            if (this.pluginOrNull(ContentNegotiation) != null) {
                println("ContentNegotiations installation skipped. Custom ContentNegotiation already installed")
            }else{
                contentConfigurationFn = {

                }
            }
        }
    }

    fun routing(block: Routing.()-> Unit){
        configRouting(block)

    }

    fun initializeWs(): Application{
        initialize()
        configHeadersPlugin()
        configContentNegotiations()
        return app
    }

    override fun setupApi(configFn : ApiConfig.()-> Unit){
        apiConfig.configFn()
    }
    override fun setupApplication(block: Application.()->Unit){
        app.block()
    }
}
package po.wswraptor.classes

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.pluginOrNull
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import po.lognotify.eventhandler.RootEventHandler
import po.lognotify.eventhandler.interfaces.CanNotify
import po.restwraptor.classes.ConfigContext
import po.restwraptor.classes.ConfigContextInterface
import po.restwraptor.models.configuration.ApiConfig
import po.wswraptor.models.configuration.WsApiConfig
import po.wswraptor.plugins.HeadersPlugin

class WSConfigContext(
    val app: Application,
    val wsConfig: WsApiConfig? = null,
    apiConfigContext: ConfigContext = ConfigContext(app)
): ConfigContextInterface by apiConfigContext, CanNotify{

    override val eventHandler = RootEventHandler("WSConfigContext")

    val wsApiConfig  = wsConfig?: WsApiConfig()

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
                install(ContentNegotiation) {
                 //   register(polymorphicConverter)
                }
            }
        }
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
package po.wswraptor.classes

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.pluginOrNull
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.Routing
import io.ktor.server.routing.routing
import po.lognotify.TasksManaged
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.restwraptor.models.configuration.ApiConfig
import po.wswraptor.models.configuration.WsApiConfig
import po.wswraptor.plugins.HeadersPlugin

class WSConfigContext(
    val app: Application,
    val wsConfig: WsApiConfig? = null
):TasksManaged{

    override val identity: CTXIdentity<out CTX> = asIdentity()

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
        configHeadersPlugin()
        configContentNegotiations()
        return app
    }

    fun setupApi(configFn : ApiConfig.()-> Unit){

    }
    fun setupApplication(block: Application.()->Unit){
        app.block()
    }


}
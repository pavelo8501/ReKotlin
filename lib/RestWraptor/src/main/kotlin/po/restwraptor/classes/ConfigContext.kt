package po.restwraptor.classes

import io.ktor.server.application.Application
import io.ktor.server.application.pluginOrNull

class ConfigContext(private  val app: Application){

    private fun configAuthentication(){
        app.apply {
//            if (this.pluginOrNull(Jwt) != null) {
//                apiLogger.info("Custom JWT installed")
//            } else {
//                installDefaultAuthentication(this)
//            }
        }
    }

}
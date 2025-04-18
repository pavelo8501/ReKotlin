package po.restwraptor.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.BaseApplicationPlugin
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.util.AttributeKey
import po.auth.authentication.jwt.JWTService


//class JWTPlugin2(private val service: JWTService) {
//    companion object Plugin : BaseApplicationPlugin<Application, JWTService, JWTPlugin2> {
//        override val key = AttributeKey<JWTPlugin2>("JwtPlugin")
//        lateinit var service : JWTService
//
//        override fun install(pipeline: Application,  serviceFn: JWTService.() -> Unit): JWTPlugin2 {
//            this.service = JWTService()
//            service.serviceFn()
//            val jwtPlugin = JWTPlugin2(service)
//            pipeline.install(Authentication) {
//                jwt(service.name) {
//                    realm = service.realm
//                    verifier(service.getVerifier())
//                    validate { credential ->
//                        return@validate  service.checkCredential(credential)
//                    }
//                }
//            }
//            return jwtPlugin
//        }
//    }
//    fun getInitializedService(): JWTService{
//        return this.service
//    }
//}
package po.restwraptor.scope

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.pluginOrNull
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.routing.routing
import po.auth.AuthSessionManager
import po.auth.authentication.authenticator.models.AuthenticationPrincipal
import po.auth.authentication.jwt.JWTService
import po.auth.authentication.jwt.models.JwtConfig
import po.auth.models.CryptoRsaKeys
import po.lognotify.TasksManaged
import po.lognotify.extensions.subTask
import po.restwraptor.extensions.respondUnauthorized
import po.restwraptor.extensions.withSession
import po.restwraptor.models.configuration.AuthConfig
import po.restwraptor.interfaces.StringHelper
import po.restwraptor.routes.configureAuthRoutes

class AuthConfigContext(
    private val application : Application,
    private val authConfig : AuthConfig
): StringHelper, TasksManaged{

    val personalName = "AuthConfigContext"

    private suspend fun installJWTAuthentication(jwtService: JWTService,  app: Application){
        app.apply {
            subTask("InstallJWTAuthentication") {handler->
                if (this.pluginOrNull(Authentication) != null) {
                    handler.info("Authentication installation skipped. Custom Authentication already installed")
                } else {
                    install(Authentication) {
                        jwt(authConfig.jwtServiceName) {
                            verifier(jwtService.getVerifier())
                            validate { credential ->
                              val principal = withSession {
                                    val jwtToken = jwtService.tokenRepository.resolve(sessionId)
                                    jwtService.isNotExpired(jwtToken){
                                        handler.info("Token not found in repository")
                                        respondUnauthorized("Session expired")
                                    }
                                    jwtService.validateToken(jwtToken)
                                }
                                principal
                            }
                        }
                    }
                    handler.info("JWT Authentication Plugin installed")
                }
            }
        }
    }

    suspend fun setupAuthentication(
        cryptoKeys: CryptoRsaKeys,
        userLookupFn: (suspend (login: String)-> AuthenticationPrincipal?)? = null
    ){
        subTask("JWT Token Config", personalName) {handler->
            authConfig.privateKey = cryptoKeys.privateKey
            authConfig.publicKey = cryptoKeys.publicKey
            authConfig.wellKnownPath = null

            val config = JwtConfig(
                realm = "ktor app",
                audience = "jwt-audience",
                issuer = "http://127.0.0.1",
                secret = "secret",
                privateKey = cryptoKeys.asRSAPrivate(),
                publicKey =  cryptoKeys.asRSAPublic()
            )
            val service = AuthSessionManager.initJwtService(config)
            if (userLookupFn != null) {
                service.setAuthenticationFn(userLookupFn)
            }

            installJWTAuthentication(service, application)
            if(authConfig.defaultSecurityRouts) {
                application.routing {
                    configureAuthRoutes(authConfig.authRoutePrefix, this@AuthConfigContext)
                }
                handler.info("AuthRoutes configured")
            }
        }
    }
}
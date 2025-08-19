package po.restwraptor.scope

import io.ktor.http.HttpStatusCode
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
import po.lognotify.launchers.runAction
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.restwraptor.extensions.respondUnauthorized
import po.restwraptor.extensions.withSession
import po.restwraptor.interfaces.WraptorResponse
import po.restwraptor.models.configuration.WraptorConfig
import po.restwraptor.routes.configureAuthRoutes
import po.restwraptor.routes.partsToUrl

class AuthConfigContext(
    private val application : Application,
    private val wraptorConfig: WraptorConfig,
): TasksManaged{

    override val identity: CTXIdentity<AuthConfigContext> = asIdentity()

    override val contextName: String = "AuthConfigContext"
    private val authConfig get() = wraptorConfig.authConfig

    private fun installJWTAuthentication(
        jwtService: JWTService,
        app: Application,
        responseProvider:()-> WraptorResponse<*>
    ){
        app.apply {
            runAction("InstallJWTAuthentication"){
                if (this@apply.pluginOrNull(Authentication) != null) {
                    notify("Authentication installation skipped. Custom Authentication already installed")
                } else {
                    install(Authentication) {
                        jwt(authConfig.jwtServiceName) {
                            verifier(jwtService.getVerifier())
                            validate { credential ->
                              val principal = withSession(this) {
                                    val jwtToken = jwtService.tokenRepository.resolve(sessionID)
                                    jwtService.isNotExpired(jwtToken){
                                        notify("Token not found in repository")
                                        respondUnauthorized("Session expired", HttpStatusCode.Unauthorized.value, responseProvider)
                                    }
                                    jwtService.validateToken(jwtToken)
                                }
                                principal
                            }
                        }
                    }
                    notify("JWT Authentication Plugin installed")
                }
            }
        }
    }

    internal fun setupAuthentication(
        cryptoKeys: CryptoRsaKeys,
        responseProvider:()-> WraptorResponse<*>,
        userLookupFn: (suspend (login: String)-> AuthenticationPrincipal?)
    ){
        runAction("JWT Token Config") {
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
            service.setAuthenticationFn(userLookupFn)

            installJWTAuthentication(service, application, responseProvider)
            if(authConfig.defaultSecurityRouts) {
                application.routing {
                    configureAuthRoutes(partsToUrl(listOf(authConfig.authRoutePrefix)), this@AuthConfigContext)
                }
                notify("AuthRoutes configured")
            }
        }
    }
}
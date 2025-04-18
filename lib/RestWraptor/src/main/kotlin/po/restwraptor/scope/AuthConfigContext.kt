package po.restwraptor.scope

import com.auth0.jwt.exceptions.JWTDecodeException
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.pluginOrNull
import io.ktor.server.auth.Authentication
import io.ktor.server.routing.routing
import po.auth.authentication.interfaces.AuthenticationPrincipal
import po.auth.authentication.jwt.JWTService
import po.auth.authentication.jwt.models.JwtConfig
import po.auth.models.CryptoRsaKeys
import po.auth.sessions.models.AuthorizedPrincipal
import po.lognotify.TasksManaged
import po.lognotify.extensions.subTask
import po.lognotify.extensions.withLastTask
import po.restwraptor.models.configuration.AuthenticationConfig
import po.restwraptor.models.request.LoginRequest
import po.restwraptor.models.security.AuthenticatedModel
import po.restwraptor.interfaces.StringHelper
import po.restwraptor.plugins.CoreAuthPlugin
import po.restwraptor.plugins.JWTPlugin
import po.restwraptor.routes.configureAuthRoutes
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

class AuthConfigContext(
    private val application : Application,
    private val configContext : ConfigContext,
): StringHelper, TasksManaged{

    val personalName = "AuthConfigContext"

    val authConfig  = AuthenticationConfig()


    private val apiConfig = configContext.apiConfig
   // private val app = configContext.a

    var onAuthenticated : ((AuthenticatedModel) -> Unit)? = null

    internal var jwtService : JWTService? = null

    private fun generateTokenForUser(user : AuthenticationPrincipal, service : JWTService): String? {
        val token =  try {
            service.generateToken(user)
        }catch (ex: JWTDecodeException ) {
            null
        }
        return token
    }

    private fun issueToken(user : AuthenticationPrincipal): String? {
        var newToken : String? = null
        this@AuthConfigContext.jwtService?.let { jwtService->
            generateTokenForUser(user, jwtService)?.let { token->
                onAuthenticated?.invoke(AuthenticatedModel(token, true, 1))
                newToken =  token
            }
        }?:run {
           // throwSkip("JWTService undefined")
        }
        return newToken
    }

//    private fun onLoginRequest(request : LoginRequest): String? {
//        request.let { loginData ->
//            credentialsValidatorFn?.let { validatorFn ->
//                val user = validatorFn.invoke(loginData)
//
//                if (user != null) {
//                   return issueToken(user)
//                }else {
//                   // warn("Login failed for ${loginData.login} with password ${loginData.password}")
//                }
//            }
//        }
//        return null
//    }

//    private fun configureRouteLogin(routing: Routing, url: String){
//        routing.apply {
//            route(url) {
//                post {
////                    eventHandler.handleUnmanagedException {
////                        respondInternal(it)
////                    }
//                   // info("Request content type: ${call.request.contentType()}")
//                    val requestText = call.receiveText()
//                    val request =  configContext.jsonFormatter.decodeFromString<LoginRequest>(
//                        requestText
//                    )
//                    val token = onLoginRequest(request)
//                    if (token != null) {
//                        call.response.header("Authorization", "Bearer $token")
//                        call.respond(ApiResponse(token))
//                    } else {
//                        call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
//                    }
//                }
//            }
//        }
//    }
//

//    private fun configureRouteRefresh(routing: Routing, url: String){
//        routing.apply {
//            route(url) {
//                post {
//                    val authHeader = call.request.headers["Authorization"]
//                    if (authHeader == null || !authHeader.startsWith("Bearer")) {
//                        respondUnauthorized("Missing or invalid token")
//                        return@post
//                    }
//                    try {
//                        jwtService?.let { service ->
//                            service.checkExpiration(authHeader){token->
//                                if(token == null){
//                                    respondUnauthorized("Invalid token data")
//                                }else{
//                                    call.response.header("Authorization", "Bearer $token")
//                                    call.respond(HttpStatusCode.OK, ApiResponse(token))
//                                }
//                            }
//                        }
//                    } catch (ex: AuthException) {
//                        respondInternal(ex)
//                        respondUnauthorized("Token expired or invalid: ${ex.message}")
//                    }
//                }
//            }
//        }
//    }
//    private fun configRouteLogout(routing: Routing, url: String){
//            routing.apply {
//            route(url) {
//                post {
//                    val request =  call.receive<ApiRequest<LogoutRequest>>()
//                    call.respond(ApiResponse(true))
//                }
//            }
//        }
//    }

//    private fun configAuthentication() {
//        app.apply {
//            if (this.pluginOrNull(Authentication) != null) {
//              //  info("Authentication installation skipped. Custom Authentication already installed")
//            } else {
//              //  info("Installing JWT Plugin")
//                val config =  JwtConfig(
//                    realm = "ktor app",
//                    audience = "jwt-audience",
//                    issuer = "http://127.0.0.1",
//                    secret = "secret",
//                    publicKeyString = authConfig.publicKeyString,
//                    privateKeyString = authConfig.privateKeyString)
//                  val plugin = install(JWTPlugin) {this.init("jwt-auth", config)}
//                  this@AutConfigContext.jwtService = plugin.getInitializedService()
//            }
//        }
//    }


    private suspend fun configCoreAuth(
        app: Application,
        privateKey: RSAPrivateKey,
        publicKey: RSAPublicKey,
        authenticatorFn: (suspend (login: String, password: String)-> AuthorizedPrincipal)? = null
    ):JWTService?{
        app.apply {

            withLastTask {handler->
                if (this.pluginOrNull(Authentication) != null) {
                    handler.info("Authentication installation skipped. Custom Authentication already installed")
                } else {
                    val config = JwtConfig(
                        realm = "ktor app",
                        audience = "jwt-audience",
                        issuer = "http://127.0.0.1",
                        secret = "secret",
                        privateKey = privateKey,
                        publicKey = publicKey
                    )
                    val service = JWTService(config)
                    service.setAuthenticationFn(authenticatorFn)
                    val plugin = install(JWTPlugin) {setup("jwt-auth", service)}
                    this@AuthConfigContext.jwtService = service
                    handler.info("JWT Plugin installed")

                    install(CoreAuthPlugin)
                    handler.info("CoreAuth Plugin installed")

                    app.routing {
                        configureAuthRoutes(authConfig.baseAuthRoute, this@AuthConfigContext)
                    }
                    handler.info("AuthRoutes configured")
                    return@withLastTask jwtService
                }
            }
        }
        return null
    }

//    private fun initializeAuthentication(){
//       // configCoreAuth()
//       // configAuthentication()
//        if(authConfig.defaultSecurityRouts){
//            app.routing{
//                configureRouteNotFoundSecured(this)
//                configureRouteLogin(this, toUrl(authConfig.baseAuthRoute, "login"))
//                configureRouteRefresh(this, toUrl(authConfig.baseAuthRoute, "refresh"))
//                configRouteLogout(this, toUrl(authConfig.baseAuthRoute, "logout"))
//            }
//        }
//    }

    suspend fun jwtConfig(
        cryptoKeys: CryptoRsaKeys,
        block: (suspend JWTService.()-> Unit)? = null
    )
    {
        var service: JWTService? = null
        subTask("JWT Token Config", personalName) {
            authConfig.privateKeyString = cryptoKeys.privateKey
            authConfig.publicKeyString = cryptoKeys.publicKey
            authConfig.wellKnownPath = null
            service =  configCoreAuth(
                application,
                cryptoKeys.asRSAPrivate(),
                cryptoKeys.asRSAPublic(),
            )
            block?.invoke(service!!)
        }
    }

//    fun applySecurity(publicKey: String, privateKey: String, validatorFn: (LoginRequest)-> AuthenticationPrincipal?)
//            = applySecurity(File(keyRoot+publicKey), File(keyRoot+privateKey), validatorFn)

//    private var keyRoot :String = ""
//    fun setKeyPath(path: String?):AuthConfigContext{
//        if(path != null && path.isNotEmpty()){
//            keyRoot = path
//        }else{
//            throw IOException("Configuration key path provided is empty")
//        }
//        return this
//    }

}
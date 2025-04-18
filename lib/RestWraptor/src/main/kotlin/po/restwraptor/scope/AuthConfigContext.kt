package po.restwraptor.scope

import com.auth0.jwt.exceptions.JWTDecodeException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.pluginOrNull
import io.ktor.server.auth.Authentication
import io.ktor.server.request.receiveText
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import po.auth.authentication.interfaces.AuthenticationPrincipal
import po.auth.authentication.jwt.JWTService
import po.auth.authentication.jwt.models.JwtConfig
import po.auth.authentication.jwt.models.RsaKeysPair
import po.restwraptor.models.configuration.AuthenticationConfig
import po.restwraptor.models.request.LoginRequest
import po.restwraptor.models.response.ApiResponse
import po.restwraptor.models.security.AuthenticatedModel
import po.restwraptor.exceptions.ExceptionCodes
import po.restwraptor.exceptions.throwConfiguration
import po.restwraptor.extensions.getOrConfigurationEx
import po.restwraptor.interfaces.StringHelper
import po.restwraptor.plugins.CoreAuthPlugin
import po.restwraptor.plugins.JWTPlugin
import po.restwraptor.routes.configureAuthRoutes
import java.io.File
import java.io.IOException

class AuthConfigContext(
    private val application : Application,
    private val configContext : ConfigContext,
): StringHelper{

    val authConfig  = AuthenticationConfig()
    var credentialsValidatorFn: ((LoginRequest)-> AuthenticationPrincipal?) ? = null

    private val apiConfig = configContext.apiConfig
   // private val app = configContext.a

    var onAuthenticated : ((AuthenticatedModel) -> Unit)? = null

    private var jwtService : JWTService? = null

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

    private fun onLoginRequest(request : LoginRequest): String? {
        request.let { loginData ->
            credentialsValidatorFn?.let { validatorFn ->
                val user = validatorFn.invoke(loginData)

                if (user != null) {
                   return issueToken(user)
                }else {
                   // warn("Login failed for ${loginData.login} with password ${loginData.password}")
                }
            }
        }
        return null
    }

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


    private fun configCoreAuth(app: Application, privateKey: String, publicKey: String){

        app.apply {
            if (this.pluginOrNull(Authentication) != null) {
                //  info("Authentication installation skipped. Custom Authentication already installed")
            } else {
                //  info("Installing JWT Plugin")
                val config = JwtConfig(
                    realm = "ktor app",
                    audience = "jwt-audience",
                    issuer = "http://127.0.0.1",
                    secret = "secret",
                )
                val service = JWTService(config)
                val plugin = install(JWTPlugin) {setup("jwt-auth", service)}

                this@AuthConfigContext.jwtService = service

                install(CoreAuthPlugin)
                app.routing {
                    configureAuthRoutes(this, authConfig.baseAuthRoute)
                }
            }
        }
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

    private fun configureRouteNotFoundSecured(routing: Routing) {

    }

    fun setRawKeys(publicKeyStr: String, privateKeyStr: String, validatorFn: (LoginRequest)-> AuthenticationPrincipal?){
        authConfig.publicKeyString = publicKeyStr
        authConfig.privateKeyString = privateKeyStr
        credentialsValidatorFn = validatorFn
        configCoreAuth(application,privateKeyStr, publicKeyStr)
    }

    fun setupJWTTokens(
        keysPair: RsaKeysPair,
        validatorFn: (LoginRequest)-> AuthenticationPrincipal?
    )
    {
            authConfig.privateKeyString = keysPair.privateKey
            authConfig.publicKeyString =   keysPair.publicKey
            authConfig.wellKnownPath = null
            credentialsValidatorFn = validatorFn
            configCoreAuth(
                application,
                authConfig.privateKeyString.getOrConfigurationEx("Private key not provided", ExceptionCodes.KEY_REGISTRATION),
                authConfig.publicKeyString.getOrConfigurationEx("Public key not provided", ExceptionCodes.KEY_REGISTRATION)
            )

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
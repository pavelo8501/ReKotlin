package po.restwraptor.classes

import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.exceptions.JWTDecodeException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install
import io.ktor.server.application.pluginOrNull
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.request.contentType
import io.ktor.server.request.receive
import io.ktor.server.request.receiveText
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import po.lognotify.eventhandler.EventHandler
import po.restwraptor.exceptions.ConfigurationErrorCodes
import po.restwraptor.exceptions.ConfigurationException
import po.restwraptor.interfaces.SecuredUserInterface
import po.restwraptor.models.configuration.AuthenticationConfig
import po.restwraptor.models.request.ApiRequest
import po.restwraptor.models.request.LoginRequest
import po.restwraptor.models.response.ApiResponse
import po.restwraptor.models.security.AuthenticatedModel
import po.restwraptor.plugins.JWTPlugin
import po.restwraptor.security.JWTService
import po.lognotify.eventhandler.EventHandlerBase
import po.lognotify.eventhandler.RootEventHandler
import po.lognotify.eventhandler.interfaces.CanNotify
import po.restwraptor.classes.convenience.respondInternal
import po.restwraptor.classes.convenience.respondUnauthorized
import po.restwraptor.exceptions.AuthException
import po.restwraptor.interfaces.StringHelper
import po.restwraptor.models.configuration.WraptorConfig
import po.restwraptor.models.request.LogoutRequest
import po.restwraptor.models.security.JwtConfig
import po.restwraptor.plugins.ReplyInterceptorPlugin
import java.io.File
import java.io.IOException

class AuthenticationContext(
    private val rootHandler : RootEventHandler,
    private val configContext : ConfigContext,
    private val  stringHelper : StringHelper = StringHelper
) : CanNotify,  StringHelper by stringHelper  {

   // private val stringHelper = StringHelper
    override val eventHandler = EventHandler("AuthenticationContext", rootHandler)

    val authConfig  = AuthenticationConfig()
    var credentialsValidatorFn: ((LoginRequest)-> SecuredUserInterface?) ? = null

    private val apiConfig = configContext.apiConfig
    private val app = configContext.app

    //    var onLoginRequest: ((LoginRequest) -> SecuredUserInterface?)?
    var onAuthenticated : ((AuthenticatedModel) -> Unit)? = null


    private var jwtService : JWTService? = null

    private fun generateTokenForUser(user : SecuredUserInterface, service : JWTService): String? {
        val token =  try {
            service.generateToken(user)
        }catch (ex: JWTDecodeException ) {
            throwSkip("Token generation failed with exception message ${ex.message.toString()}")
            null
        }
        return token
    }

//    if (authConfig.useWellKnownHost) {
//        TODO("Use well known hosts logic not implemented")
//        propagatedException<ConfigurationException>("Token generation failed") {
//            errorCode = ConfigurationErrorCodes.PLUGIN_SETUP_FAILURE
//        }


    private fun issueToken(user : SecuredUserInterface): String? {
        var newToken : String? = null
        this@AuthenticationContext.jwtService?.let { jwtService->
            generateTokenForUser(user,jwtService)?.let { token->
                onAuthenticated?.invoke(AuthenticatedModel(token, true, 1))
                newToken =  token
            }
        }?:run {
            throwSkip("JWTService undefined")
        }
        return newToken
    }

    private fun onLoginRequest(request : ApiRequest<LoginRequest>): String? {
        request.data.let { loginData ->
            credentialsValidatorFn?.let { validatorFn ->
                val user = validatorFn.invoke(loginData)
                if (user != null && user.password == request.data.password) {
                   return issueToken(user)
                }else {
                    warn("Login failed for ${loginData.login} with password ${loginData.password}")
                }
            }
        }
        return null
    }

    private fun configureRouteLogin(routing: Routing, url: String){
        routing.apply {
            route(url) {
                post {
                    task("$url request") {
                        info("Request content type: ${call.request.contentType()}")
                        val requestText = call.receiveText()
                        val request = Json.decodeFromString<ApiRequest<LoginRequest>>(requestText)
                        val token = onLoginRequest(request)
                        if (token != null) {
                            call.response.header("Authorization", "Bearer $token")
                            call.respond(ApiResponse(token))
                        } else {
                            call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                        }
                    }
                }
            }
        }
    }
    private fun configureRouteRefresh(routing: Routing, url: String){
        routing.apply {
            route(url) {
                post {
                    val authHeader = call.request.headers["Authorization"]
                    if (authHeader == null || !authHeader.startsWith("Bearer")) {
                        respondUnauthorized("Missing or invalid token")
                        return@post
                    }
                    try {
                        jwtService?.let { service ->
                            service.checkExpiration(authHeader){token->
                                if(token == null){
                                    respondUnauthorized("Invalid token data")
                                }else{
                                    call.response.header("Authorization", "Bearer $token")
                                    call.respond(HttpStatusCode.OK, ApiResponse(token))
                                }
                            }
                        }
                    } catch (ex: AuthException) {
                        respondInternal(ex)
                        respondUnauthorized("Token expired or invalid: ${ex.message}")
                    }
                }
            }
        }
    }
    private fun configRouteLogout(routing: Routing, url: String){
        routing.apply {
            route(url) {
                post {
                    val request =  call.receive<ApiRequest<LogoutRequest>>()
                    call.respond(ApiResponse(true))
                }
            }
        }
    }

    private fun configAuthentication() {
        app.apply {
            if (this.pluginOrNull(Authentication) != null) {
                info("Authentication installation skipped. Custom Authentication already installed")
            } else {
                info("Installing JWT Plugin")
                val config =  JwtConfig(
                    realm = "ktor app",
                    audience = "jwt-audience",
                    issuer = "http://127.0.0.1",
                    secret = "secret",
                    publicKeyString = authConfig.publicKeyString,
                    privateKeyString = authConfig.privateKeyString)
                  val plugin = install(JWTPlugin) {this.init("jwt-auth", config)}
                  this@AuthenticationContext.jwtService = plugin.getInitializedService()
//                install(ReplyInterceptorPlugin){
//                    injectService(this@AuthenticationContext.jwtService!!)
//                }
            }
        }
    }

    private fun initializeAuthentication(){
        configAuthentication()
        if(authConfig.defaultSecurityRouts){
            app.routing{
                configureRouteLogin(this, toUrl(authConfig.baseAuthRoute, "login"))
                configureRouteRefresh(this, toUrl(authConfig.baseAuthRoute, "refresh"))
                configRouteLogout(this, toUrl(authConfig.baseAuthRoute, "logout"))
            }
        }
    }

    fun applySecurity(
        publicKeyHandler: File,
        privateKeyHandler: File,
        validatorFn: (LoginRequest)-> SecuredUserInterface?
    ){
        if(publicKeyHandler.exists() && privateKeyHandler.exists()){
            publicKeyHandler.bufferedReader().use { reader ->
                authConfig.publicKeyString = reader.readText()
            }
            privateKeyHandler.bufferedReader().use { reader ->
                authConfig.privateKeyString = reader.readText()
            }
            authConfig.wellKnownPath = null
            credentialsValidatorFn = validatorFn
            initializeAuthentication()
        }else{
            throw IOException("Security keys not found")
        }
    }

    fun applySecurity(publicKey: String, privateKey: String, validatorFn: (LoginRequest)-> SecuredUserInterface?)
            = applySecurity(File(keyRoot+publicKey), File(keyRoot+privateKey), validatorFn)

    private var keyRoot :String = ""
    fun setKeyPath(path: String?):AuthenticationContext{
        if(path != null && path.isNotEmpty()){
            keyRoot = path
        }else{
            throw IOException("Configuration key path provided is empty")
        }
        return this
    }

}
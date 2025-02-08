package po.restwraptor.classes

import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install
import io.ktor.server.application.pluginOrNull
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.request.contentType
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
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
import po.restwraptor.models.configuration.WraptorConfig
import po.restwraptor.models.security.JwtConfig
import po.restwraptor.plugins.ReplyInterceptorPlugin
import java.io.File
import java.io.IOException

class AuthenticationContext(
    private val configContext : ConfigContext
) : CanNotify {

    init {
        val authenticationContext  = "LOL"
    }


    override val eventHandler: EventHandlerBase = RootEventHandler("AuthenticationContext")

    val authConfig  = AuthenticationConfig()
    var credentialsValidatorFn: ((LoginRequest)-> SecuredUserInterface?) ? = null

    private val apiConfig = configContext.apiConfig
    private val app = configContext.app

    //    var onLoginRequest: ((LoginRequest) -> SecuredUserInterface?)?
    var onAuthenticated : ((AuthenticatedModel) -> Unit)? = null


    private var jwtService : JWTService? = null

    private fun generateTokenForUser(user : SecuredUserInterface, service : JWTService): String {
        val token =  try {
            service.generateToken(user)
        }catch (ex: Exception) {
            println(ex.message)
            throw ex
        }
        return token
    }

//    if (authConfig.useWellKnownHost) {
//        TODO("Use well known hosts logic not implemented")
//        propagatedException<ConfigurationException>("Token generation failed") {
//            errorCode = ConfigurationErrorCodes.PLUGIN_SETUP_FAILURE
//        }


    private fun issueToken(user : SecuredUserInterface): String {
        this@AuthenticationContext.jwtService?.let { jwtService->
            val token =  generateTokenForUser(user,jwtService)
            onAuthenticated?.invoke(AuthenticatedModel(token, true, 1))
            return token
        }?:run {
            throw Exception("jwtService undefined")
        }
    }

    private fun onLoginRequest(request : ApiRequest<LoginRequest>): String? {
        request.data.let { loginData ->
            credentialsValidatorFn?.let { validatorFn ->
                val user = validatorFn.invoke(loginData)
                if (user != null) {
                   return issueToken(user)
                }else {
                    warn("Login failed for ${loginData.login} with password ${loginData.password}")
                }
            }
        }
        return null
    }

    private fun configureRouteLogin(routing: Routing){
        routing.apply {
            route("${apiConfig.baseApiRoute}/login") {
                post {
                    try {
                        info("Request content type: ${call.request.contentType()}")
                        val token = onLoginRequest(call.receive<ApiRequest<LoginRequest>>())
                        if (token != null) {
                            call.response.header("Authorization", "Bearer $token")
                            call.respond(ApiResponse(token))
                        } else {
                            call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                        }
                    }catch (ex: Exception){
                        respondInternal(ex)
                    }
                }
            }
        }
    }
    private fun configureRouteRefresh(routing: Routing){
        routing.apply {
            route("${apiConfig.baseApiRoute}/refresh") {
                post {
                    val authHeader = call.request.headers["Authorization"]
                    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
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
                install(ReplyInterceptorPlugin){
                    injectService(this@AuthenticationContext.jwtService!!)
                }
            }
        }
    }

    private fun initializeAuthentication(){
        configAuthentication()
        if(authConfig.defaultSecurityRouts){
            app.routing{
                configureRouteLogin(this)
                configureRouteRefresh(this)
            }
        }
    }

    fun applySecurity(
        publicKeyHandler: File,
        privateKeyHandler: File,
        validatorFn: (LoginRequest)-> SecuredUserInterface?){
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
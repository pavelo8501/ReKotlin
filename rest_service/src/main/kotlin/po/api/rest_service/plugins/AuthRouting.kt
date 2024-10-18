package po.api.rest_service.plugins


/*
fun Application.configureRouting() {
    val generalService by inject<GeneralService>()
    val issuer = ConfigService.returnConfig("config.json").host
    val audience = environment.config.property("jwt.audience").getString()
    val privateKeyString = generalService.getPrivateKey()
    val publicKeyString = generalService.getPublicKey()

    JWTService.configure(privateKeyString, publicKeyString, audience, issuer)

    routing {
        route("/api/auth/login") {
            post {
                val request = Json.decodeFromString<ApiServiceRequest>(call.receiveText())
                val user = generalService.authenticate(request.caller, request.value)
                if (user != null) {
                    val token = JWTService.generateToken(user)
                    if (token != null) {
                        call.respond(hashMapOf("token" to token))
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, "Authentication failed")
                    }
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                }
            }
        }

        route("/api/auth/logout") {
            post {
                call.respond(HttpStatusCode.OK, "Logout successful")
            }
        }

        authenticate("jwt-auth") {
            route("/api/secure") {
                get {
                    call.respond(HttpStatusCode.OK, "Secured endpoint")
                }
            }
        }

        // Serve public keys for token verification (optional)
        staticFiles(".well-known/jwks.json", File("certs/jwks.json"))
    }
}
*/


/*
Auth usage example:

authenticate("jwt-auth") {
    route("/api/secure") {
        get {
            call.respondText("This is a secured endpoint!")
        }
    }
}*/
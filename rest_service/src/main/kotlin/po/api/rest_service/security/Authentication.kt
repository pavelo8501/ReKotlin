package po.api.rest_service.security

/*
install(Authentication) {
    jwt("jwt-auth") {
        verifier(JWTService.getVerifier())
        validate { credentials ->
            if (credentials.payload.audience.contains(environment.config.property("jwt.audience").getString())) {
                JWTPrincipal(credentials.payload)
            } else null
        }
        challenge { _, _ -> call.respond(HttpStatusCode.Unauthorized, "Token is not valid or expired") }
    }
}*/
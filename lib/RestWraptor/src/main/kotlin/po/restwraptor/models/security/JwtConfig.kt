package po.restwraptor.models.security

import com.auth0.jwk.JwkProvider
//
//data class JwtConfig(
//    var claimFieldName: String = "login",
//    var realm: String = "Access to secure API",
//    var audience: String = "jwt-audience",
//    var issuer: String = "http://0.0.0.0:8080/",
//    var secret: String = "secret",
//    var publicKeyString: String? = null,
//    var privateKeyString: String? = null,
//    var kid: String?  = null,
//    var jwkProvider: JwkProvider? = null
//) {
//    fun setKeys(publicKeyString: String, privateKeyString: String) {
//        this.publicKeyString = publicKeyString
//        this.privateKeyString = privateKeyString
//    }
//
//    fun setJwkProvider(jwkProvider: JwkProvider, privateKeyString: String, kid : String ) {
//        this.jwkProvider = jwkProvider
//        this.privateKeyString = privateKeyString
//        this.kid = kid
//    }
//}
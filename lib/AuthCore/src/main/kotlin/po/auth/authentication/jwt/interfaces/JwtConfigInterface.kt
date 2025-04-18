package po.auth.authentication.jwt.interfaces

import com.auth0.jwk.JwkProvider

interface JwtConfigInterface {
   // var claimFieldName:String
    var realm: String
    var issuer: String
    var secret: String
    var kid: String?
    var audience: String

    fun setJwkProvider(jwkProvider: JwkProvider, privateKeyString: String, kid : String)
}


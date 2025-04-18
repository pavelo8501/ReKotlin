package po.auth.authentication.jwt.interfaces

import com.auth0.jwk.JwkProvider
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

interface JwtConfigInterface {
   // var claimFieldName:String
    var realm: String
    var issuer: String
    var secret: String
    var kid: String?
    var audience: String

    val privateKey: RSAPrivateKey
    val publicKey: RSAPublicKey

    fun setJwkProvider(jwkProvider: JwkProvider, privateKeyString: String, kid : String)
}


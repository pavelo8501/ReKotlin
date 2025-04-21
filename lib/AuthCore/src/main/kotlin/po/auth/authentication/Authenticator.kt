package po.auth.authentication

import okio.Path
import po.auth.authentication.exceptions.AuthException
import po.auth.authentication.exceptions.ErrorCodes
import po.auth.authentication.jwt.JWTService
import po.auth.authentication.jwt.models.JwtConfig
import po.auth.models.CryptoRsaKeys
import po.auth.sessions.models.AuthorizedPrincipal
import java.security.KeyPairGenerator
import java.security.PublicKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

object Authenticator {

    var keyBasePath:  Path? = null
    //var jwtService : JWTService? = null

    fun generateRsaKeys(keySize: Int = 2048): CryptoRsaKeys {
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(keySize)
        val keyPair = keyGen.generateKeyPair()

        return CryptoRsaKeys(
            privateKey = keyPair.private as RSAPrivateKey,
            publicKey = keyPair.public as RSAPublicKey
        )
    }





//    fun initJwtService(privateKey: RSAPrivateKey, publicKey: RSAPublicKey):JWTService{
//        if(jwtService == null){
//            jwtService = JWTService(JwtConfig(
//                privateKey = privateKey,
//                publicKey = publicKey
//            ))
//        }
//        return jwtService!!
//    }
//
//    fun initJwtService(config: JwtConfig):JWTService{
//        if(jwtService == null){
//            jwtService = JWTService(config)
//        }
//        return jwtService!!
//    }

}
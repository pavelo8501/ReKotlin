package po.auth.authentication

import okio.Path
import po.auth.authentication.exceptions.AuthException
import po.auth.authentication.exceptions.ErrorCodes
import po.auth.models.CryptoRsaKeys
import po.auth.sessions.models.AuthorizedPrincipal
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

object Authenticator {

    var keyBasePath:  Path? = null

    fun generateRsaKeys(keySize: Int = 2048): CryptoRsaKeys {
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(keySize)
        val keyPair = keyGen.generateKeyPair()

        return CryptoRsaKeys(
            privateKey = keyPair.private as RSAPrivateKey,
            publicKey = keyPair.public as RSAPublicKey
        )
    }

    var authFn : (suspend (login: String, password: String)-> AuthorizedPrincipal)? = null
    fun setAuthenticator(callback: suspend (login: String, password: String)-> AuthorizedPrincipal){
        authFn = callback
    }

    suspend fun authenticate(login: String, password: String): AuthorizedPrincipal{
       return  authFn?.invoke(login, password) ?:run {
          throw AuthException("Authenticate function not set", ErrorCodes.CONFIGURATION_MISSING)
       }
    }


}
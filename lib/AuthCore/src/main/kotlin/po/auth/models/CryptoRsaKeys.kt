package po.auth.models

import po.auth.exceptions.AuthErrorCode
import po.auth.exceptions.AuthException
import po.auth.exceptions.authException
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64


class CryptoRsaKeys(
    val privateKey: PrivateKey,
    val publicKey: PublicKey
) {

    fun asRSAPrivate():RSAPrivateKey{
        return privateKey as RSAPrivateKey
    }

    fun asRSAPublic(): RSAPublicKey{
        return publicKey as RSAPublicKey
    }

    companion object {
        fun fromPem(privatePem: String, publicPem: String): CryptoRsaKeys {
            val keyFactory = KeyFactory.getInstance("RSA")

            runCatching {
                val privateBytes = decodePem(privatePem)
                val privateSpec = PKCS8EncodedKeySpec(privateBytes)
                keyFactory.generatePrivate(privateSpec)
            }.onFailure {
                throw authException("Decoding privateKey", AuthErrorCode.INVALID_KEY_FORMAT, it)
            }.onSuccess {privateKey->
                runCatching {
                    val publicBytes = decodePem(publicPem)
                    val publicSpec = X509EncodedKeySpec(publicBytes)
                    keyFactory.generatePublic(publicSpec)
                }.onFailure {
                    throw authException("Decoding publicKey", AuthErrorCode.INVALID_KEY_FORMAT, it)
                }.onSuccess {publicKey->
                    return CryptoRsaKeys(privateKey, publicKey)
                }
            }
            throw authException("CryptoRsaKeys FromPem failed", AuthErrorCode.INVALID_KEY_FORMAT, null)
        }

        private fun decodePem(pem: String): ByteArray {
            return pem
                .replace("-----BEGIN .*KEY-----".toRegex(), "")
                .replace("-----END .*KEY-----".toRegex(), "")
                .replace("\\s".toRegex(), "")
                .let { Base64.getDecoder().decode(it) }
        }
    }
}
package po.auth.authentication.extensions

import okio.Path.Companion.toPath
import po.auth.models.CryptoRsaKeys
import okio.FileSystem
import okio.buffer
import po.auth.authentication.Authenticator
import java.security.PrivateKey
import java.security.PublicKey
import java.util.Base64


fun generateRsaKeys(keySize: Int =  2048): CryptoRsaKeys{
   return Authenticator.generateRsaKeys(keySize)
}

fun PrivateKey.toPem(): String {
    val encoded = this.encoded
    val base64 = Base64.getMimeEncoder(64, "\n".toByteArray()).encodeToString(encoded)
    return "-----BEGIN PRIVATE KEY-----\n$base64\n-----END PRIVATE KEY-----"
}

fun PublicKey.toPem(): String {
    val encoded = this.encoded
    val base64 = Base64.getMimeEncoder(64, "\n".toByteArray()).encodeToString(encoded)
    return "-----BEGIN PUBLIC KEY-----\n$base64\n-----END PUBLIC KEY-----"
}

fun CryptoRsaKeys.writeToDisk(baseDir: String = ".", privateName: String = "private.pk8", publicName: String = "public.spki") {
    val privatePath = "$baseDir/$privateName".toPath()
    val publicPath = "$baseDir/$publicName".toPath()

    FileSystem.SYSTEM.sink(privatePath).buffer().use { it.writeUtf8(privateKey.toPem()) }
    FileSystem.SYSTEM.sink(publicPath).buffer().use { it.writeUtf8(publicKey.toPem()) }
}

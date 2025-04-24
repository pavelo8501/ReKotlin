package po.auth.extensions


import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import po.auth.AuthSessionManager
import po.auth.authentication.jwt.models.RsaKeysPair
import po.auth.models.CryptoRsaKeys
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.Base64


fun currentPath(): Path {
    val currentDir = System.getProperty("user.dir")
    val path = currentDir.toPath()
    if (FileSystem.SYSTEM.exists(path)) {
        AuthSessionManager.authenticator.keyBasePath = path
        return path
    } else {
        throw IllegalArgumentException("Current path does not exist: $currentDir")
    }
}

fun setKeyBasePath(basePath: String): Path {
    val path = if (basePath.startsWith("/")) {
        basePath.toPath()
    } else {
        currentPath().resolve(basePath)
    }
    if (!FileSystem.SYSTEM.exists(path)) {
        throw IllegalArgumentException("Provided path does not exist: $path")
    }
    AuthSessionManager.authenticator.keyBasePath  = path
    return path
}

fun setKeyBasePath(basePath: Path): Path {
    if (!FileSystem.SYSTEM.exists(basePath)) {
        throw IllegalArgumentException("Provided path does not exist: $basePath")
    }
    AuthSessionManager.authenticator.keyBasePath  = basePath
    return basePath
}

private fun readRsaKeys(privateKeyFileName: String, publicKeyFileName: String, basePath: Path): RsaKeysPair {
    basePath.apply{
        val privateKey = FileSystem.SYSTEM.source(resolve(privateKeyFileName)).buffer().use { it.readUtf8() }
        val publicKey = FileSystem.SYSTEM.source(resolve(publicKeyFileName)).buffer().use { it.readUtf8() }
        return RsaKeysPair(privateKey, publicKey)
    }
}

private fun readRsaKeys(
    privateKeyFileName: String,
    publicKeyFileName: String,
    basePath: String? = null
): RsaKeysPair {
    val base: Path = basePath?.let { setKeyBasePath(it) } ?: setKeyBasePath(currentPath())
    return readRsaKeys(privateKeyFileName, publicKeyFileName, base)
}

fun Path.readCryptoRsaKeys(privateKeyFileName: String, publicKeyFileName: String): CryptoRsaKeys {
    val privateKey = FileSystem.SYSTEM.source(resolve(privateKeyFileName)).buffer().use { it.readUtf8() }
    val publicKey = FileSystem.SYSTEM.source(resolve(publicKeyFileName)).buffer().use { it.readUtf8() }
    return CryptoRsaKeys.fromPem(privateKey,publicKey)
}

fun readCryptoRsaKeys(
    privateKeyFileName: String,
    publicKeyFileName: String,
    basePath: String? = null
): CryptoRsaKeys {
    val pair = readRsaKeys(privateKeyFileName, publicKeyFileName, basePath)
    return CryptoRsaKeys.fromPem(pair.privateKey, pair.publicKey)
}

fun generateRsaKeys(keySize: Int =  2048): CryptoRsaKeys{
    val keyGen = KeyPairGenerator.getInstance("RSA")
    keyGen.initialize(keySize)
    val keyPair = keyGen.generateKeyPair()

    return CryptoRsaKeys(
        privateKey = keyPair.private as RSAPrivateKey,
        publicKey = keyPair.public as RSAPublicKey
    )
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

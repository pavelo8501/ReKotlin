package po.auth.authentication.extensions


import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import po.auth.authentication.Authenticator

import po.auth.authentication.jwt.models.RsaKeysPair

fun validateCredentials(login: String, password: String){
    Authenticator.validateCredentials(login, password)
}

fun setKeyBasePath(basePath: String): Path {
    val path  = basePath.toPath()
    Authenticator.keyBasePath = basePath.toPath()
    if (FileSystem.SYSTEM.exists(path)) {
        Authenticator.keyBasePath = path
        return  path
    }else{
        throw IllegalArgumentException("Provided path does not exist: $basePath")
    }
}

//fun Path.readRsaKeys(privateKeyFileName: String, publicKeyFileName: String):RsaKeysPair{
//    val basePath = this
//    val privateKeyPath = basePath.resolve(privateKeyFileName)
//    val publicKeyPath = basePath.resolve(publicKeyFileName)
//    val privateKey =  FileSystem.SYSTEM.source(privateKeyPath).buffer().use{ it.readUtf8() }
//    val publicKey = FileSystem.SYSTEM.source(publicKeyPath).buffer().use{ it.readUtf8() }
//    return RsaKeysPair(privateKey, publicKey)
//}

fun readRsaKeys(privateKeyFileName: String, publicKeyFileName: String): RsaKeysPair {

    val basePath = Authenticator.keyBasePath

    val privateKeyPath = basePath?.resolve(privateKeyFileName) ?: privateKeyFileName.toPath()
    val publicKeyPath = basePath?.resolve(publicKeyFileName) ?: publicKeyFileName.toPath()

    val privateKey =  FileSystem.SYSTEM.source(privateKeyPath).buffer().use{ it.readUtf8() }
    val publicKey = FileSystem.SYSTEM.source(publicKeyPath).buffer().use{ it.readUtf8() }

    return RsaKeysPair(privateKey, publicKey)
}
package po.misc.io

import po.misc.data.helpers.output
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths


class ReadFileHooks<R: Any>{

    internal var onErrorCallback: ((Throwable) -> Unit)? = null
    fun onError(callback: (Throwable) -> Unit){
        onErrorCallback = callback
    }

    internal fun triggerError(throwable: Throwable){
        onErrorCallback?.invoke(throwable)
    }

    internal var success: ((LocalFile) -> R)? = null
    fun onSuccess(callback: (LocalFile) -> R){
        success = callback
    }
    internal fun triggerSuccess(localFile : LocalFile):R?{
       return success?.invoke(localFile)
    }
}


fun fileExists(relativePath: String): FileMeta?{
    val file = File(relativePath)
    return try {
        if (file.exists() && file.isFile) {
            FileMeta(
                file
            )
        } else null
    } catch (e: SecurityException) {
        e.output()
        null
    }
}


fun readFileContent(
    relativePath: String,
):ByteArray{
    val path = Paths.get(System.getProperty("user.dir")).resolve(relativePath)
    return Files.readAllBytes(path)
}


fun readFile(relativePath: String): LocalFile{
    val file = File(System.getProperty("user.dir"), relativePath)
    return  LocalFile(file.readBytes(), FileMeta(file))
}



fun <R: Any> readFile(
    relativePath: String,
    withHooks: ReadFileHooks<R>.()-> Unit
):R?{
    val hooks = ReadFileHooks<R>().apply(withHooks)
    try {
       val localFile =  readFile(relativePath)
       return hooks.triggerSuccess(localFile)
    } catch (th: Throwable) {
       hooks.triggerError(th)
       return null
    }
}



fun readResourceContent(path: String): String {
    return object {}.javaClass.classLoader.getResource(path)
        ?.readText()
        ?: error("Resource not found: $path")
}

fun ByteArray.readToString(charset: Charset = Charsets.UTF_8): String = toString(charset)


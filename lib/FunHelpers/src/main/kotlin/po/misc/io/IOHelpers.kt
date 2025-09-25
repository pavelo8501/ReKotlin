package po.misc.io


import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintStream
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists


enum class FileType{
    Text,
    Image
}


fun readFileContent(
    relativePath: String,
):ByteArray{
    val path = Paths.get(System.getProperty("user.dir")).resolve(relativePath)
    return Files.readAllBytes(path)
}

fun readFileContent(
    relativePath: String,
    withHooks: FileIOHooks.()-> Unit
):ByteArray?{

    val hooks = FileIOHooks().apply(withHooks)
    try {
       return readFileContent(relativePath)
    }catch (th: Throwable){
        hooks.onErrorCallback?.invoke(FileIOError(th, relativePath))
        return null
    }
}


fun ByteArray.readToString(charset: Charset = Charsets.UTF_8): String = toString(charset)

fun ByteArray.bytesToText(charset: Charset = Charsets.UTF_8): String = toString(charset)


fun readResourceContent(path: String): String {
    return object {}.javaClass.classLoader.getResource(path)
        ?.readText()
        ?: error("Resource not found: $path")
}


data class OutputResult<R: Any>(
    val output:String,
    val result:R
){
    fun printOutput(){
        println(output)
    }
}

inline fun <reified R : Any> captureOutput(
    crossinline captureLambda: () -> R
): OutputResult<R> {
    val originalOut = System.out
    val outputStream = ByteArrayOutputStream()
    System.setOut(PrintStream(outputStream))

    try {
        val result = captureLambda()
        return OutputResult(outputStream.toString(), result)
    }catch (th: Throwable){
        throw th
    }
    finally {
        System.setOut(originalOut)
    }
}


package po.misc.io

import po.misc.data.PrettyPrint
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.colorize
import po.misc.exceptions.throwableToText
import java.io.IOException
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

data class WriteOptions(
    val overwriteExistent: Boolean = true,
    val createSubfolders: Boolean = true,
    val throwOnFileExists: Boolean = false
)

data class FileIOError(
    val throwable:Throwable,
    val path: String
): PrettyPrint{

    override val formattedString: String
        get() = this.toString().colorize(Colour.Red)

    override fun toString(): String {
        return "FileWriteError"+ SpecialChars.NewLine.char + "${throwable.throwableToText()} for Path: $path"
    }
}

class FileIOHooks{
    var onErrorCallback: ((FileIOError) -> Unit)? = null
    fun onError(callback: (FileIOError) -> Unit){
        onErrorCallback = callback
    }
}


private fun writeFile(
    relativePath: String,
    content: String,
    options: WriteOptions
):FileIOError?{

    return try {
        val pathToFile = Path(System.getProperty("user.dir"), relativePath)
        val directory = pathToFile.parent

        if (!directory.exists() && options.createSubfolders) {
            directory.createDirectories()
        }

        if (pathToFile.exists() && !options.overwriteExistent) {
            if(options.throwOnFileExists){
                throw IOException("File already exists: $relativePath")
            }else{
                return null
            }
        }

        Files.writeString(pathToFile, content)
        null
    } catch (th: Throwable) {
        FileIOError(th, relativePath)
    }
}


fun String.writeFileContent(
    relativePath: String,
    options: WriteOptions = WriteOptions(),
    withHooks: FileIOHooks.()-> Unit
): Boolean {
    val hooks = FileIOHooks()
    hooks.withHooks()
    return writeFile(relativePath, this, options)?.let { result ->
        hooks.onErrorCallback?.invoke(result)
        false
    } ?: true
}

fun String.writeFileContent(
    relativePath: String,
    options: WriteOptions = WriteOptions()
): Boolean {
    return writeFile(relativePath, this, options) == null
}


private fun writeFile(
    relativePath: String,
    byteArray: ByteArray,
    options: WriteOptions
):FileIOError?{
    return try {
        val pathToFile = Path(System.getProperty("user.dir"), relativePath)
        val directory = pathToFile.parent

        if (!directory.exists() && options.createSubfolders) {
            directory.createDirectories()
        }
        if (pathToFile.exists() && !options.overwriteExistent) {
            if(options.throwOnFileExists) {
                throw IOException("File already exists: $relativePath")
            }else{
                return null
            }
        }
        Files.write(pathToFile, byteArray)
        null
    } catch (th: Throwable) {
        FileIOError(th, relativePath)
    }
}

fun ByteArray.writeFileContent(
    relativePath: String,
    options: WriteOptions = WriteOptions(),
    withHooks: FileIOHooks.()-> Unit
): Boolean {
    val hooks = FileIOHooks()
    hooks.withHooks()
    return writeFile(relativePath, this, options)?.let { result ->
        hooks.onErrorCallback?.invoke(result)
        false
    } ?: true
}

fun ByteArray.writeFileContent(
    relativePath: String,
    options: WriteOptions = WriteOptions()
): Boolean {
    return writeFile(relativePath, this, options) == null
}






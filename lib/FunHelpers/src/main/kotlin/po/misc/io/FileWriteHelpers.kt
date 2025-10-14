package po.misc.io

import po.misc.data.PrettyPrint
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.colorize
import po.misc.exceptions.throwableToText
import po.misc.types.getOrManaged
import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

data class WriteOptions(
    val overwriteExistent: Boolean = false,
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
        return "FileWriteError"+ SpecialChars.newLine + "${throwable.throwableToText()} for Path: $path"
    }
}

class FileIOHooks{

    internal var onErrorCallback: ((FileIOError) -> Unit)? = null
    fun onError(callback: (FileIOError) -> Unit){
        onErrorCallback = callback
    }
    fun triggerError(error:FileIOError): Boolean{
       return onErrorCallback?.let {
            it.invoke(error)
            true
        }?:false
    }
    internal var onSuccessCallback: ((ByteArray) -> ByteArray)? = null

    fun onSuccess(callback: (ByteArray) -> ByteArray): Unit{
        onSuccessCallback = callback
    }

    internal var onResultCallback: ((ByteArray) -> Any)? = null

    fun <R> onResult(callback: (ByteArray) -> ByteArray): Unit{
        onResultCallback = callback
    }

    fun triggerSuccess(array: ByteArray): Boolean{
        return onSuccessCallback?.let {
            it.invoke(array)
            true
        }?:false
    }
}


class FileIOHooks2<R: Any>{
    internal var onErrorCallback: ((FileIOError) -> Unit)? = null
    fun onError(callback: (FileIOError) -> Unit){
        onErrorCallback = callback
    }
    fun triggerError(error:FileIOError): Boolean{
        return onErrorCallback?.let {
            it.invoke(error)
            true
        }?:false
    }

    internal var onResultCallback: ((FileMeta) -> R)? = null

    fun onResult(callback: (FileMeta) -> R): Unit{
        onResultCallback = callback
    }

    fun triggerOnResult(file: FileMeta): R {
      return  onResultCallback.getOrManaged(this).invoke(file)
    }
}


class WriteFileHooks<R: Any>{

    internal var onErrorCallback: ((Throwable) -> Unit)? = null
    fun onError(callback: (Throwable) -> Unit){
        onErrorCallback = callback
    }

    @PublishedApi
    internal fun triggerError(throwable: Throwable){
        onErrorCallback?.invoke(throwable)
    }

    internal var success: ((LocalFile) -> R)? = null
    fun onSuccess(callback: (LocalFile) -> R){
        success = callback
    }
    @PublishedApi
    internal fun triggerSuccess(localFile : LocalFile):R?{
        return success?.invoke(localFile)
    }
}


private fun writeFileContents(
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

fun writeFile(
    relativePath: String,
    bytes: ByteArray,
    options: WriteOptions = WriteOptions()
): LocalFile{
        val file = File(System.getProperty("user.dir"), relativePath)
        val pathToFile = file.toPath()
        val directory = pathToFile.parent

        if (!directory.exists() && options.createSubfolders) {
            directory.createDirectories()
        }

        if (pathToFile.exists() && !options.overwriteExistent) {
            throw IOException("File already exists: $relativePath")
        }
        file.writeBytes(bytes)
        return  LocalFile(bytes, FileMeta(relativePath, file))
}

inline fun <R: Any> writeFile(
    relativePath: String,
    bytes: ByteArray,
    options: WriteOptions = WriteOptions(overwriteExistent = false, createSubfolders = true),
    writeFileHooks: WriteFileHooks<R>.() ->  Unit
): R? {
    val hooks = WriteFileHooks<R>().apply(writeFileHooks)
    try {
        val localFile = writeFile(relativePath, bytes, options)
        return  hooks.triggerSuccess(localFile)
    }catch (th: Throwable){
        hooks.triggerError(th)
        return null
    }
}

fun ByteArray.writeToFile(
    relativePath: String,
    options: WriteOptions = WriteOptions()
):LocalFile {
    return writeFile(relativePath, this,  options)
}


fun <R: Any> ByteArray.writeToFile(
    relativePath: String,
    options: WriteOptions = WriteOptions(),
    writeFileHooks: WriteFileHooks<R>.() ->  Unit
):R? {
    return writeFile<R>(relativePath, this,  options, writeFileHooks)
}





fun String.writeFileContent(
    relativePath: String,
    options: WriteOptions = WriteOptions(),
    withHooks: FileIOHooks.()-> Unit
): Boolean {
    val hooks = FileIOHooks()
    hooks.withHooks()
    return writeFileContents(relativePath, this, options)?.let { result ->
        hooks.onErrorCallback?.invoke(result)
        false
    } ?: true
}

fun String.writeFileContent(
    relativePath: String,
    options: WriteOptions = WriteOptions()
): Boolean {
    return writeFileContents(relativePath, this, options) == null
}


private fun writeFileContent(
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
    return writeFileContent(relativePath, this, options)?.let { result ->
        hooks.onErrorCallback?.invoke(result)
        false
    } ?: true
}

fun ByteArray.writeFileContent(
    relativePath: String,
    options: WriteOptions = WriteOptions()
): Boolean {
    return writeFileContent(relativePath, this, options) == null
}






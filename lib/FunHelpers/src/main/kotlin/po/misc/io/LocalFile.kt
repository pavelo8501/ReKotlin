package po.misc.io

import po.misc.data.output.output
import po.misc.exceptions.throwableToText
import po.misc.time.TimeHelper
import java.nio.charset.Charset
import java.time.Instant


class LocalFile(
    val bytes: ByteArray,
    val meta: FileMetaData,
) : FileMetaData by meta, TimeHelper {

    fun readText(charset: Charset = Charsets.UTF_8): String = file.readText(charset)

    override fun readBytes(): ByteArray {
        return bytes
    }

    fun rewrite(withBytes: ByteArray? = null): Boolean{
        val byteArray = withBytes?:bytes
      return  try {
            file.writeBytes(byteArray)
            true
        }catch (th: Throwable){
            th.throwableToText().output()
            false
        }
    }
}

class SourcedFile<T: Any>(
    val bytes: ByteArray,
    val meta: FileMetaData,
    val provider: (ByteArray)-> T
) : FileMetaData by meta, TimeHelper {

    val firstCreated: Instant = nowTimeUtc()

    constructor(localFile: LocalFile, provider: (ByteArray)-> T):this(localFile.bytes, localFile.meta, provider)

    private var sourceBacking:T? = null

    val source: T get() {
       return sourceBacking ?:run {
            val fromProvider = provider(bytes)
            sourceBacking = fromProvider
            fromProvider
        }
    }



    override fun readBytes(): ByteArray {
        return bytes
    }

    fun updateSource(source: T, bytes: ByteArray): Boolean{
       return try {
            file.writeBytes(bytes)
            sourceBacking = source
            true
        }catch (th: Throwable){
            th.throwableToText().output()
            false
        }
    }

}

//inline fun <reified T: Any> SourcedFile<T>.update(string: String): SourcedFile<T>?{
//    return try {
//         source.writeSourced(relativePath, Charsets.UTF_8, WriteOptions(overwriteExistent = true)){
//            string
//        }
//    }catch (th: Throwable){
//        th.throwableToText().output()
//        null
//    }
//}
//
//
//inline fun <reified T: Any> SourcedFile<T>.update(bytes: ByteArray):  SourcedFile<T>?{
//    return try {
//        source.writeSourced(relativePath, WriteOptions(overwriteExistent = true)){
//            bytes
//        }
//    }catch (th: Throwable){
//        th.throwableToText().output()
//        null
//    }
//}
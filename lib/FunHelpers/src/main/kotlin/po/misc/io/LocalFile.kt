package po.misc.io

import po.misc.time.TimeHelper
import java.io.File


class LocalFile(
    val bytes: ByteArray,
    val meta: FileMetaData,
) : FileMetaData by meta {

    override fun readBytes(): ByteArray {
        return bytes
    }
}



class SourcedFile<T: Any>(
    val bytes: ByteArray,
    file: FileMetaData,
    val provider: (ByteArray)-> T
) : FileMetaData by file, TimeHelper {

    constructor(localFile: LocalFile, provider: (ByteArray)-> T):this(localFile.bytes,localFile.meta, provider)

    val source: T by lazy { provider(bytes) }

    override fun readBytes(): ByteArray {
        return bytes
    }
}


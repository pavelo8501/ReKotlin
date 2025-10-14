package po.misc.io

import java.io.File


class LocalFile(
    val bytes: ByteArray,
    file: FileMetaData,
) : FileMetaData by file {


    override fun readBytes(): ByteArray {
        return bytes
    }

}
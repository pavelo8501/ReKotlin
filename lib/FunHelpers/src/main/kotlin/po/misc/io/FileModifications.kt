package po.misc.io

import po.misc.data.helpers.output
import po.misc.exceptions.throwableToText
import java.nio.file.Files
import java.nio.file.Paths


fun deleteFile(relativePath: String): Boolean{

    try {
        val path = Paths.get(System.getProperty("user.dir")).resolve(relativePath)
        Files.delete(path)
        return true
    }catch (th: Throwable){
        th.throwableToText().output()
        return false
    }
}
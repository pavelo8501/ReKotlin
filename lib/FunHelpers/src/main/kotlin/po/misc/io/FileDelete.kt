package po.misc.io

import po.misc.data.output.output
import po.misc.exceptions.throwableToText
import po.misc.functions.Throwing
import java.io.File
import java.nio.file.FileSystemException
import java.nio.file.Files
import java.nio.file.Paths



class DeletionList(){
    internal val relPathsToDelete = mutableListOf<String>()
    fun addPath(relativePath: String){
        relPathsToDelete.add(relativePath)
    }
}

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


private fun executeDeleteAllOrNan(
    throwing: Throwing?,
    block:DeletionList.() -> Unit
): Boolean{
    val list = DeletionList()
    list.block()
    val files = mutableListOf<Pair<String, File>>()
    for(relPath in  list.relPathsToDelete){
        fileExists(relPath)?.let {meta->
            files.add(Pair(relPath,meta.file))
        }?:run {
            if(throwing != null){
                throw FileSystemException("File : $relPath does not exist")
            }
            return false
        }
    }
    files.forEach {
        it.second.delete()
    }
    return true
}


fun deleteAllOrNan(throwing: Throwing,  block:DeletionList.() -> Unit): Unit {
    executeDeleteAllOrNan(throwing, block)
}

fun deleteAllOrNan(block:DeletionList.() -> Unit): Boolean = executeDeleteAllOrNan(null, block)
package po.misc.io


import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


fun readFileContent(relativePath: String): String {
    val path: Path = Paths.get(System.getProperty("user.dir")).resolve(relativePath)
    return Files.readString(path)
}

fun readResourceContent(path: String): String {
    return object {}.javaClass.classLoader.getResource(path)
        ?.readText()
        ?: error("Resource not found: $path")
}
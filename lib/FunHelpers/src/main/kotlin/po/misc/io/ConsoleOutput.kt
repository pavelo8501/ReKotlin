package po.misc.io

import java.io.ByteArrayOutputStream
import java.io.PrintStream

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
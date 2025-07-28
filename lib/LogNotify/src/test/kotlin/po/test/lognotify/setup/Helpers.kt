package po.test.lognotify.setup

import java.io.ByteArrayOutputStream
import java.io.PrintStream


internal data class OutputResult<R: Any>(
    val output:String,
    val result:R
)


internal fun captureOutput(captureLambda:()-> Unit): String{
    val originalOut = System.out
    val outputStream = ByteArrayOutputStream()
    System.setOut(PrintStream(outputStream))
    try {
        captureLambda()
    } finally {
        System.setOut(originalOut)
    }
    return outputStream.toString()
}


internal inline fun <reified R : Any> captureOutput(
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
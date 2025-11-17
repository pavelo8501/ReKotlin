package po.misc.io

import po.misc.functions.LambdaType
import po.misc.functions.Nullable
import java.io.ByteArrayOutputStream
import java.io.PrintStream

data class OutputResult<R>(
    val output:String,
    val result:R,
    val exception: Throwable? = null
){
    fun printOutput(){
        println(output)
    }
}

inline fun <reified R> captureOutput(
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


inline fun <reified R: Any?> captureOutput(
    nullable: Nullable,
    crossinline captureLambda: () -> R
): OutputResult<out R?> {
    val originalOut = System.out
    val outputStream = ByteArrayOutputStream()
    System.setOut(PrintStream(outputStream))
    try {
        val result = captureLambda()
        return OutputResult(outputStream.toString(), result)
    }catch (th: Throwable){
        val result = OutputResult(outputStream.toString(), null, th)
        return result
    }
    finally {
        System.setOut(originalOut)
    }
}



suspend inline fun <reified R : Any> captureOutput(
    suspend: LambdaType.Suspended,
    crossinline  captureLambda: suspend () -> R
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
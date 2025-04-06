package po.managedtask.extensions


import po.managedtask.exceptions.DefaultException
import po.managedtask.exceptions.ExceptionBase
import po.managedtask.exceptions.enums.DefaultType

inline fun <reified T: Any> Any.safeCast(): T? {
    return this as? T
}

fun <T: Any> T?.getOrThrow(ex : ExceptionBase): T{
    return this ?: throw ex
}

fun <T: Any> T?.getOrThrowDefault(message: String): T{
    val defaultException = DefaultException(message, DefaultType.DEFAULT)
    return this ?: throw defaultException
}

inline fun <T: Any> T?.letOrThrow(ex : ExceptionBase, block: (T)-> T): Unit{
    if(this != null){
        block(this)
    } else {
        throw ex
    }
}

inline fun <T: Any> T?.letOrThrow(message: String, code: Int,  processableBuilderFn: (String, Int) -> ExceptionBase, block: (T)-> T): T{
    return if (this != null) block(this) else throw processableBuilderFn.invoke(message, code)
}


package po.lognotify.extensions


import po.lognotify.exceptions.ManagedException

inline fun <reified T: Any> Any.safeCast(): T? {
    return this as? T
}


inline fun <reified T: Any, E: ManagedException> Any.castOrException(exception:E): T {
    val result =  this as? T
    return result?:throw exception
}

fun <T: Any, E: ManagedException> T?.getOrException(exception : E): T{
    return this ?: throw exception
}

inline fun <T: Any> T?.letOrException(ex : ManagedException, block: (T)-> T){
    if(this != null){
        block(this)
    } else {
        throw ex
    }
}

fun <T: Any?, E: ManagedException> T.testOrException( exception : E, predicate: (T) -> Boolean): T{
    if (predicate(this)){
        return this
    }else{
        throw exception
    }
}

inline fun <reified T> Iterable<T>.countEqualsOrException(equalsTo: Int, exception:ManagedException):Iterable<T>{

    val actualCount = this.count()
    if(actualCount != equalsTo){
        throw exception
    }else{
        return this
    }
}



//fun <E: ExceptionBase>  E.throwCancel(msg: String= "", handler: HandlerType = HandlerType.CANCEL_ALL){
//    CancellationException("${this.message} $msg", handler)
//}

//fun <T: Any> T?.getOrThrow(ex : ExceptionBase): T{
//    return this ?: throw ex
//}
//
//fun Boolean.trueOrThrow(message: String): Boolean{
//    if(!this){
//       throw DefaultException("Tested value is false $message", HandlerType.SKIP_SELF)
//    }
//    return true
//}

//fun <T: Any> T?.getOrThrowDefault(message: String): T{
//    val defaultException = DefaultException(message, HandlerType.SKIP_SELF)
//    return this ?: throw defaultException
//}
//
//fun <T: Any> T?.getOrDefault(defaultValue: T): T{
//    return this ?: return defaultValue
//}
//
//fun <T: Any> T?.getOrThrowCancellation(message: String, handlerType : HandlerType = HandlerType.SKIP_SELF): T{
//    val defaultException = CancellationException(message, handlerType)
//    return this ?: throw defaultException
//}
//
//inline fun <T: Any> T?.letOrThrow(ex : ExceptionBase, block: (T)-> T){
//    if(this != null){
//        block(this)
//    } else {
//        throw ex
//    }
//}

//inline fun <T: Any> T?.letOrThrow(
//    message: String,
//    code: Int,
//    processableBuilderFn: (String, Int) -> ExceptionBase, block: (T)-> T): T
//{
//    return if (this != null) block(this) else throw processableBuilderFn.invoke(message, code)
//}


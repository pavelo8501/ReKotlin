package po.lognotify.extensions


import po.lognotify.exceptions.ManagedException
import kotlin.reflect.KClass

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

inline fun <T1 : Any, R : Any> safeLet(p1: T1?, block: (T1) -> R?): R? {
    return if (p1 != null) block(p1) else null
}


inline fun <reified T: Any> getType(): KClass<T> {
    return T::class
}

inline fun <reified T, reified U> initializeContexts(
    receiverInstance: T,
    paramInstance: U,
    block: T.(U) -> Unit
) {
    receiverInstance.block(paramInstance)
}

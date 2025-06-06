package po.misc.exceptions

import po.misc.data.console.helpers.emptyOnNull
import po.misc.data.console.helpers.wrapByDelimiter

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

fun Throwable.toInfoString(): String{
    val base = this.javaClass.simpleName
    val msg = message ?: ""
    val cause = cause?.let { " | Cause: ${it.javaClass.simpleName} - ${it.message ?: "No message"}" } ?: ""
    return "$base: $msg$cause"
}

fun ManagedException.waypointInfo(): String{
  return  handlingData.asReversed().joinToString(" -> "){ "${it.wayPoint.personalName}[${it.event}]" }
       .wrapByDelimiter("->")
}

fun Throwable.exceptionName(): String{
    return if(this is ManagedException){
        "${selfIdentifiable.completeName}(${selfIdentifiable.personalName})[${handler.name}]"
    }else{
        "${this.javaClass.simpleName} (${message ?: ""})}"
    }
}

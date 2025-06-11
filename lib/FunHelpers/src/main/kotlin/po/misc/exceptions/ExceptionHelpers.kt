package po.misc.exceptions

import po.misc.data.helpers.emptyOnNull
import po.misc.data.helpers.wrapByDelimiter

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


inline fun <reified EX: ManagedException> Throwable.toManaged(message: String, source: Enum<*>): EX{
    return SelfThrownException.build<EX>(message, source, this)
}

fun Throwable.toInfoString(): String{
    val base = this.javaClass.simpleName
    val msg = message ?: ""
    val cause = cause?.let { " | Cause: ${it.javaClass.simpleName} - ${it.message ?: "No message"}" } ?: ""
    return "$base: $msg$cause"
}

fun ManagedException.waypointInfo(): String{
  return  handlingData.asReversed().joinToString(" -> "){ "${it.wayPoint.sourceName}[${it.event}]" }
       .wrapByDelimiter("->")
}

fun <EX: Throwable> EX.name(): String{
    return if (this is ManagedException) {
        val name = this::class.simpleName
        val handlerName = this.handler.name
        "$name[msg:${message} ${source?.name.emptyOnNull("code:")} hdl:${handlerName}]"
    }else{
        "${this.javaClass.simpleName}[msg:${message ?: ""}]"
    }
}

fun <EX: Throwable> EX.shortName(): String{
    return if (this is ManagedException) {
        val name = this::class.simpleName
        val handlerName = this.handler.name
        val completeName = "$name(${message})"
        completeName
    }else{
        "${this.javaClass.simpleName}(${message ?: ""})"
    }
}

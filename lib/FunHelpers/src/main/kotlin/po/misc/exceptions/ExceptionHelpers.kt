package po.misc.exceptions

import po.misc.context.CTX
import po.misc.data.helpers.wrapByDelimiter
import po.misc.exceptions.models.ExceptionData
import po.misc.context.Identifiable


fun CTX.managedException(message: String, source: Enum<*>?, original: Throwable?): ManagedException{
    val exceptionMessage = "$message @ $completeName"
    return  ManagedException(exceptionMessage, source, original)
}


fun throwManaged(message: String, handler : HandlerType?, source: Enum<*>? , original: Throwable?): Nothing{
    if(handler == null){
        val exception =  ManagedException(message)
        throw exception
    }else{
        val exception =  ManagedException(message)
        exception.handler = handler
        throw exception
    }
}

fun throwManaged(message: String, handler : HandlerType? = null): Nothing{
    if(handler == null){
        throw ManagedException(message)
    }else{
      val exception =  ManagedException(message)
        exception.handler = handler
        throw exception
    }
}

inline fun <reified EX: ManagedException, S: Enum<S>> throwManageable(
    message: String,
    source: S? = null,
    context: CTX? = null
): Nothing{
    val managedException : EX = ManageableException.build<EX, S>(message, source)
    if(context != null){
        managedException.throwSelf(context, ManagedException.ExceptionEvent.Thrown)
    }else{
        throw  managedException
    }
}

fun ManagedCallSitePayload.toManaged(): ManagedException{
  return  ManagedException.create(this)
}

fun Throwable.toManaged(ctx: CTX, handler: HandlerType, source: Enum<*>?): ManagedException{
    val exceptionMessage = "$message @ ${ctx.completeName}"
    val exception = ctx.managedException(exceptionMessage,source, this)
    exception.handler = handler
    return exception
}

fun Throwable.toManaged(payload: ManagedCallSitePayload): ManagedException{

    val exceptionMessage = "$message @ ${payload.producer.completeName}"
    val managed = ManagedException(this.throwableToText(), payload.source, this)
    payload.handler?.let {
        managed.handler = it
    }
    return managed
}

inline fun <reified EX: ManagedException, S: Enum<S>> Throwable.toManageable(ctx: CTX, source: S): EX{
    val exceptionMessage = "$message @ ${ctx.completeName}"
    return ManageableException.build<EX, S>(exceptionMessage, source, this)
}

fun Throwable.toInfoString(): String{
    val base = this.javaClass.simpleName
    val msg = message ?: ""
    val cause = cause?.let { " | Cause: ${it.javaClass.simpleName} - ${it.message ?: "No message"}" } ?: ""
    return "$base: $msg$cause"
}

fun ManagedException.waypointInfo(): String{
  val resultStr =  handlingData.joinToString(" -> "){ "$it" }
       .wrapByDelimiter(delimiter =  "->", maxLineLength = 200)
    return resultStr
}

fun  Throwable.throwableToText(): String{
   return if(this.message != null){
        this.message.toString()
    }else{
        this.javaClass.simpleName.toString()
    }
}

//fun <EX: Throwable> EX.shortName(): String{
//    return if (this is ManagedException) {
//        val name = this::class.simpleName
//        val handlerName = this.handler.name
//        val completeName = "$name(${message})"
//        completeName
//    }else{
//        "${this.javaClass.simpleName}(${message ?: ""})"
//    }
//}

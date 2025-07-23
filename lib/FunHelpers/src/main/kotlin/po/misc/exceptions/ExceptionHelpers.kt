package po.misc.exceptions

import po.misc.context.CTX
import po.misc.data.helpers.wrapByDelimiter
import po.misc.exceptions.models.ExceptionData
import po.misc.context.Identifiable


fun CTX.managedException(message: String, source: Enum<*>?): ManagedException{

    val exceptionMessage = "$message @ $completeName"
    val payload =   toPayload {
        this.message = exceptionMessage
        this.code = source
    }
    return  ManagedException(payload)
}

fun CTX.managedException(cause: Throwable): ManagedException{
    val payload = toPayload(cause)
    return  ManagedException(payload)
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


fun Throwable.toManaged(ctx: CTX, handler: HandlerType): ManagedException{

   val payload =  ctx.toPayload{  }
   return  ManagedException(payload)
}

fun Throwable.toManaged(payload: ExceptionPayload): ManagedException{
    val exceptionMessage = "$message @ ${payload.producer?.completeName}"
    payload.message = exceptionMessage
    val managed = ManagedException(payload)
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

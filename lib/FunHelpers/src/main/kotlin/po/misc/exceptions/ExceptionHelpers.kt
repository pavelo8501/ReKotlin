package po.misc.exceptions

import po.misc.data.helpers.emptyOnNull
import po.misc.data.helpers.wrapByDelimiter
import po.misc.interfaces.IdentifiableContext

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


inline fun <reified EX: ManagedException, S: Enum<S>>  IdentifiableContext.manageableException(
    message: String,
    source: S,
    original: Throwable? = null
):EX{
    val exceptionMessage = "$message @ $contextName"
    return ManageableException.build<EX, S>(exceptionMessage, source, original)
}

fun IdentifiableContext.managedException(message: String, source: Enum<*>?, original: Throwable?): ManagedException{
    val exceptionMessage = "$message @ $contextName"
    return  ManagedException(exceptionMessage, source, original)
}

fun IdentifiableContext.managedWithHandler(message: String, handler: HandlerType,  source: Enum<*>): ManagedException{
    val exception = ManagedException(message, source, null)
    exception.handler  = handler
    return exception
}



fun throwManaged(message: String, ctx: IdentifiableContext,  handler : HandlerType? = null): Nothing{
    if(handler == null){
        val exception =  ManagedException(message)
        exception.throwSelf(ctx)
    }else{
        val exception =  ManagedException(message)
        exception.handler = handler
        exception.throwSelf(ctx)
    }
}


fun throwManaged(payload: ManagedCallSitePayload): Nothing{
    if(payload.handler == null){
        throw ManagedException(payload.message)
    }else{
        val exception =  ManagedException(payload.message)
        exception.handler = payload.handler
        throw exception
    }
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
    ctx: IdentifiableContext? = null
): Nothing{
    val managedException : EX = ManageableException.build<EX, S>(message, source)
    if(ctx != null){
        managedException.throwSelf(ctx)
    }else{
        throw  managedException
    }
}


fun Throwable.toManaged(ctx: IdentifiableContext,  handler: HandlerType,  source: Enum<*>?): ManagedException{
    val exceptionMessage = "$message @ ${ctx.contextName}"
    val exception = ctx.managedException(exceptionMessage,source, this)
    exception.handler = handler
    return exception
}

inline fun <reified EX: ManagedException, S: Enum<S>> Throwable.toManageable(ctx: IdentifiableContext, source: S): EX{
    val exceptionMessage = "$message @ ${ctx.contextName}"
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

fun <EX: Throwable> EX.name(): String{
    return if (this is ManagedException) {
        val name = this::class.simpleName
        val handlerName = this.handler.name
        "$name[msg:${message} ${source?.name.emptyOnNull("code:")} hdl:${handlerName}]"
    }else{
        "${this.javaClass.simpleName}[msg:${message ?: ""}]"
    }
}


fun  Throwable.text(): String{
   return if(this.message != null){
        this.message.toString()
    }else{
        this.javaClass.simpleName.toString()
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

package po.misc.functions

import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.exceptions.exceptionPayload
import po.misc.exceptions.toManaged
import po.misc.functions.models.ProbeObject
import po.misc.interfaces.IdentifiableContext
import po.misc.interfaces.ObservedContext
import po.misc.reflection.classes.ClassRole
import po.misc.reflection.classes.overallInfo

inline fun <T, reified R: Any> T.methodeProbe(
    methodName: String,
    vararg inputParameters: Any,
    block: ProbeContext<T>.()->R
):R where  T: ObservedContext{

   return try {
        val probeObject = ProbeObject(this)
        val payload =  exceptionPayload("Exception in Method: $methodName of Component: $completeName", HandlerType.SKIP_SELF, null,  exceptionOutput)
       probeObject.provideExPayload(payload)
       block.invoke(probeObject)
    }catch (throwable: Throwable){
       val resultInfo = overallInfo<R>(ClassRole.Result)
       val receiverInfo = this.overallInfo(ClassRole.Receiver){
           implements(this@methodeProbe,  IdentifiableContext::class)
           implements(this@methodeProbe,  ObservedContext::class)
       }
       inputParameters.forEach {
           val resultInfo = it.overallInfo(ClassRole.MethodParameter){}
       }
        when(throwable) {
            is ManagedException -> {
                exceptionOutput?.let {
                    it(throwable)
                    println(throwable.message)
                } ?: throw throwable
            }
            else -> {
                val exception = throwable.toManaged(this, HandlerType.SKIP_SELF, null)
                exceptionOutput?.let {
                    it(exception)
                    println(exception.message)
                } ?: throw exception
            }
        }
       return Unit as R
    }
}
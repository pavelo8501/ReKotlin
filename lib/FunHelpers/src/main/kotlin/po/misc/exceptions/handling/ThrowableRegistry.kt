package po.misc.exceptions.handling

import po.misc.collections.maps.ClassKeyedMap
import po.misc.data.helpers.output
import po.misc.data.styles.Colour
import po.misc.types.helpers.simpleOrNan
import po.misc.types.safeCast
import kotlin.reflect.KClass


sealed interface LambdaType

object Suspended : LambdaType


sealed interface LambdaContainer<T: Any, R>{
    suspend fun triggerSuspended(value:T):R
    fun trigger(value:T):R
}


class ThrowableRegistry {
    data class SuspendedLambda(val suspended:  suspend (Throwable) -> Nothing): LambdaContainer<Throwable, Nothing>{
        override suspend fun triggerSuspended(value:Throwable):Nothing{
            suspended.invoke(value)
        }
        override fun trigger(value:Throwable): Nothing{
           throw Exception("sss")
        }
    }

    data class NoSuspendLambda(val nonSuspending: (Throwable) -> Nothing, ):LambdaContainer<Throwable, Nothing>{
        override suspend fun triggerSuspended(value:Throwable):Nothing{
            nonSuspending.invoke(value)
        }
        override fun trigger(value:Throwable):Nothing{
            nonSuspending.invoke(value)
        }
    }

    val terminationHandlers: ClassKeyedMap<KClass<out Throwable>, LambdaContainer<Throwable, Nothing>> =  ClassKeyedMap()
    inline fun  <reified TH: Throwable> registerNoReturn(lambdaType: Suspended, noinline  lambda: suspend (TH)-> Nothing){
        lambda.safeCast<suspend (Throwable)-> Nothing >()?.let { casted->
            terminationHandlers[TH::class] =  SuspendedLambda(suspended =  casted)
        }?:run {
            "Cast failure for ThrowableLambda<${TH::class.simpleOrNan()}, T>".output(Colour.YellowBright)
        }
    }

    inline fun  <reified TH: Throwable> registerNoReturn(
        noinline  lambda: (TH)-> Nothing
    ){
        lambda.safeCast<(Throwable)-> Nothing >()?.let { casted->
            terminationHandlers[TH::class] =  NoSuspendLambda(nonSuspending = casted)
        }?:run {
            "Cast failure for ThrowableLambda<${TH::class.simpleOrNan()}, T>".output(Colour.YellowBright)
        }
    }

    fun  <TH: Throwable> dispatch(throwable:TH): Nothing{
        val handlerFound =  terminationHandlers[throwable::class]
       if(handlerFound != null){
            "Handler for  suspended lambda  ${throwable::class.simpleOrNan()} found, providing.".output(Colour.GreenBright)
            handlerFound.trigger(throwable)
        }else{
            "No handler registered for suspended lambda  ${throwable::class.simpleOrNan()}. Rethrowing".output(Colour.YellowBright)
            throw throwable
        }
    }
    suspend fun  <TH: Throwable> dispatch(throwable:TH,lambdaType:  Suspended): Nothing{
       val handlerFound =  terminationHandlers[throwable::class]
        if(handlerFound != null){
            handlerFound.triggerSuspended(throwable)
        }else{
            "No handler registered for suspended lambda  ${throwable::class.simpleOrNan()}. Rethrowing".output(Colour.YellowBright)
            throw throwable
        }
    }
}
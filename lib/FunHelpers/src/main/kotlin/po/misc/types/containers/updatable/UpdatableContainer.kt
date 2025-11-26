package po.misc.types.containers.updatable

import po.misc.callbacks.CallbackManager
import po.misc.callbacks.Containable
import po.misc.callbacks.builders.callbackManager
import po.misc.callbacks.builders.createPayload
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.exceptions.ManagedException
import po.misc.exceptions.toManaged
import po.misc.context.asSubIdentity
import po.misc.reflection.classes.ClassInfo
import po.misc.reflection.classes.ClassRole
import po.misc.reflection.classes.overallInfo
import po.misc.reflection.classes.overallInfoFromType
import po.misc.types.containers.ComplexContainers
import po.misc.types.containers.TypedContainer
import po.misc.types.containers.updatable.models.UpdatableData
import po.misc.types.containers.updatable.models.UpdatableEvents
import po.misc.types.token.TypeToken
import kotlin.reflect.KMutableProperty1


class UpdatableContainer<T: CTX, R: Any, V: Any>(
    val source:T,
    typeData: TypeToken<T>,
    containerTypeData: TypeToken<UpdatableContainer<T, R, V>>,
    classInfo: ClassInfo<T>,
    val property: KMutableProperty1<R, V>,
    val dataLambda:(T)-> V
): TypedContainer<T>(source, typeData, classInfo), ComplexContainers<T>, DeferredMutation<T, R>, CTX {

    override val identity = asSubIdentity(containerTypeData,  source)

    override val notifier: CallbackManager<UpdatableEvents> = callbackManager<UpdatableEvents>(
        { createPayload<UpdatableEvents,UpdatableData>(UpdatableEvents.OnArmed) },
        { createPayload<UpdatableEvents,UpdatableData>(UpdatableEvents.UpdateInvoked) },
        { createPayload<UpdatableEvents,UpdatableData>(UpdatableEvents.Failure) },
    )
    var  updateLambda: (()-> R)? = null

    private fun createData(message: String, success: Boolean, event: UpdatableEvents):UpdatableData{
        return UpdatableData(
            event = event,
            receiver = receiver,
            message = message,
            ok = success)
    }

    private fun notifyUpdateLambda(){
        var message = "UpdateLambda:"
        message += if(updateLambda != null){ "Available"
        }else{ "Null" }
        val event = UpdatableEvents.OnArmed
        notifier.trigger<UpdatableData>(UpdatableEvents.OnArmed, createData(message, updateLambda!=null, event))
    }

    fun provideUpdateLambda(updateProvider:()-> R){
        updateLambda = updateProvider
        notifyUpdateLambda()
    }

    override fun triggerUpdate(controlMessage: String) {
        val event = UpdatableEvents.UpdateInvoked
        val data = dataLambda.invoke(receiver)
        updateLambda?.let {
            val value = it.invoke()
            property.set(value, data)
            notifier.trigger<UpdatableData>(UpdatableEvents.UpdateInvoked, createData(controlMessage, true, event))
        }?:run {
            val message = "triggerUpdateModification failed. ModificationLambda not provided"
            notifier.trigger<ManagedException>(UpdatableEvents.Failure, ManagedException(source, message) )
        }
    }

    override fun triggerUpdate(value: R) {
        var event = UpdatableEvents.UpdateInvoked
        val result = runCatching {
            val data = dataLambda.invoke(receiver)
            property.set(value, data)
            notifier.trigger<UpdatableData>(event, createData("By direct value provided", true, event))
        }.onFailure {
            event = UpdatableEvents.Failure

            notifier.trigger<ManagedException>(event, it.toManaged(this) )
        }
    }
}

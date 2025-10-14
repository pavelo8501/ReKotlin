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
import po.misc.types.type_data.TypeData
import po.misc.types.containers.ComplexContainers
import po.misc.types.containers.TypedContainer
import po.misc.types.containers.updatable.models.UpdatableData
import po.misc.types.containers.updatable.models.UpdatableEvents
import po.misc.types.token.TypeToken
import kotlin.reflect.KMutableProperty1


interface UpdatableClass<T: Any>{
    val activeValue:T
    fun update(value:T)
}

class ActionValue<V>(
  private val owner: CTX,
  override val identity: CTXIdentity<out  CTX>,
  private val actionLambda: (Containable<ActionClassData<V>>)-> Unit
): ComplexContainers<V>, CTX where V:Any{

    enum class ActionClassEvents{
        OnValueProvided
    }
    data class ActionClassData<V: Any>(val event: ActionClassEvents, val value:V, val exception: ManagedException?){
        val success: Boolean = exception == null
    }
   // override val identity = asSubIdentity<>(owner)

    val actionClassNotifier: CallbackManager<ActionClassEvents> = CallbackManager<ActionClassEvents>(ActionClassEvents::class.java, this)
    internal val onValuePayload = CallbackManager.createPayload<ActionClassEvents, ActionClassData<V>>(actionClassNotifier, ActionClassEvents.OnValueProvided)

    init {
        onValuePayload.subscribe(owner, actionLambda)
    }

    fun provideValue(value: V) {
        val data = ActionClassData(ActionClassEvents.OnValueProvided, value, null)
        onValuePayload.triggerForAll(data)
    }
}

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


inline fun <reified T: CTX, reified R: Any, reified V: Any> T.toUpdatableContainer(
    property: KMutableProperty1<R, V>,
    noinline  dataLambda:(T)-> V
):UpdatableContainer<T, R, V>{

    val containerData = TypeToken.create<UpdatableContainer<T, R, V>>()

    return UpdatableContainer(this, TypeToken.create<T>(), containerData,  overallInfo(ClassRole.Receiver), property, dataLambda)
}

fun <T: CTX, R: Any, V: Any> T.toUpdatableContainer(
    typeData: TypeToken<T>,
    containerTypeData: TypeToken<UpdatableContainer<T, R, V>>,
    property: KMutableProperty1<R, V>,
    dataLambda:(T)-> V
):UpdatableContainer<T, R, V>{
   val info = overallInfoFromType<T>(ClassRole.Receiver, typeData.kType)

    return UpdatableContainer(this, typeData, containerTypeData,  info,  property,  dataLambda)
}

package po.misc.containers

import po.misc.context.tracable.TraceableContext
import po.misc.functions.hooks.Change
import po.misc.functions.models.NotificationConfig
import po.misc.types.token.TypeToken


sealed interface LazyContainerBuilder<T: Any>{

    private val thisAsContainer: LazyContainer<T> get() = this as LazyContainer<T>

    fun buildConfig(block: NotificationConfig.()-> Unit){
        thisAsContainer.config.block()
    }

    fun onValueProvided(callback: (T)-> Unit){
        thisAsContainer.onValueProvided = callback
    }

    fun provide(valueProvider: ()->T):LazyContainer<T>

}

class LazyContainer<T: Any>(
    host: Any,
    typeData: TypeToken<T>
):BackingContainerBase<T>(host, typeData), LazyContainerBuilder<T> {

    override val emissionType: EmissionType = EmissionType.EmmitOnce
    var lockedForEdit: Boolean = false
        private set

    internal var config: NotificationConfig = NotificationConfig()

    internal var onValueProvided: ((T)-> Unit)? = null

    override fun valueAvailable(value:T){
        lockedForEdit = true
        onValueProvided?.invoke(value)
    }

    override fun provide(valueProvider: () -> T): LazyContainer<T> {
        super<BackingContainerBase>.provide(valueProvider)
        return this
    }

    fun onValueSet(callback:(Change<T?, T>)-> Unit){
        changedHook.subscribe(callback)
    }



//    fun requestValue(subscriber: CTX, callback:(T)-> Unit){
//        backingValue?.let {
//            callback.invoke(it)
//        }?:run {
//            registry.subscribe(subscriber, callback)
//        }
//    }

//    override fun provideValue(value: T):LazyContainer<T> {
//        if(!lockedForEdit){
//            super.provideValue(value)
//            if(registry.subscriptionsCount> 0){
//                registry.trigger(value)
//            }
//            lockedForEdit = true
//            registry.clear()
//        }
//        return this
//    }


    companion object{
        inline operator fun <reified T: Any> invoke(
            host: Any,
        ): LazyContainer<T>{
          return  LazyContainer(host, TypeToken.create<T>())
        }
    }
}

inline fun <reified T: Any> TraceableContext.lazyContainerOf(

):LazyContainer<T>{
    return LazyContainer(this, TypeToken.create<T>())
}

fun <T: Any> TraceableContext.lazyContainerOf(
    typeToken: TypeToken<T>
):LazyContainer<T>{
    return LazyContainer(this, typeToken)
}

inline fun <reified T: Any> TraceableContext.lazyContainer(
    builder: LazyContainerBuilder<T>.()-> Unit
):LazyContainer<T>{
    val container = lazyContainerOf<T>()
    container.builder()
    return container
}

fun <T: Any> TraceableContext.lazyContainer(
    typeToken: TypeToken<T>,
    builder: LazyContainerBuilder<T>.()-> Unit
):LazyContainer<T>{
    val container = lazyContainerOf(typeToken)
    container.builder()
    return container
}



package po.misc.containers


import po.misc.containers.BackingContainerBase.EmissionType
import po.misc.context.tracable.TraceableContext
import po.misc.functions.models.NotificationConfig

interface BackingBuilder<T: Any>{
    val config: NotificationConfig

    fun valueProvided(listener: TraceableContext, callback: (T)-> Unit)
    fun provideValue(type : EmissionType = EmissionType.EmmitAlways, valueProvider:() ->T):BackingContainerBase<T>
    fun provideValue(newValue:T, allowOverwrite: Boolean):BackingContainerBase<T>

    fun setFallback(provider: ()->T)

    fun buildConfig(block: NotificationConfig.()-> Unit){
        config.block()
    }
}
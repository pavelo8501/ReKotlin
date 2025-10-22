package po.misc.configs.hocon

import com.typesafe.config.Config
import po.misc.callbacks.common.EventHost
import po.misc.callbacks.event.HostedEvent
import po.misc.configs.hocon.builders.ResolverBuilder
import po.misc.configs.hocon.models.ActivityLog
import po.misc.configs.hocon.models.HoconEntry
import po.misc.configs.hocon.models.HoconEntryBase
import po.misc.configs.hocon.models.HoconListEntry
import po.misc.configs.hocon.models.HoconNestedEntry
import po.misc.configs.hocon.models.HoconNullableEntry
import po.misc.configs.hocon.models.HoconPrimitives
import po.misc.configs.hocon.models.HoconString
import po.misc.context.component.Component
import po.misc.context.component.ComponentID
import po.misc.context.component.componentID
import po.misc.data.helpers.output
import po.misc.data.styles.Colour
import po.misc.types.helpers.simpleOrAnon
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty

interface HoconResolvable<T: HoconResolvable<T>>{
    val resolver: HoconResolver<T>
}

interface HoconConfigurable<T: EventHost, C: HoconResolvable<C>, V: Any> : HoconResolvable<C>{
    val receiver:T
    override val resolver: HoconResolver<C>
    val hoconPrimitive: HoconPrimitives<V>
}

class HoconResolver<C: HoconResolvable<C>>(
    val typeToken: TypeToken<C>
): EventHost, Component{

    override val componentID: ComponentID = componentID()

    @PublishedApi
    internal val entryResolved: MutableMap<KProperty<*>, HostedEvent<*, *, Unit>> =  mutableMapOf()
    @PublishedApi
    internal val members: MutableList<HoconResolver<*>> = mutableListOf()
    @PublishedApi
    internal  val entries: MutableList<HoconEntryBase<C, *>> = mutableListOf()


    fun registerMember(resolvable: HoconResolvable<*>): HoconResolver<C>{
        members.add(resolvable.resolver)
        return this
    }

    fun register(hoconEntry: HoconEntryBase<C, *>): Boolean{
        entries.add(hoconEntry)
        return true
    }


    fun readConfig(
        config: C,
        hoconFactory: Config
    ): List<ActivityLog>{
        for(hoconEntry in entries){
            "readConfig for for class ${config::class.simpleOrAnon}. Processing entry $hoconEntry".output(Colour.Green)
            when(hoconEntry){
                is HoconEntry ->{
                    hoconEntry.readConfig(config, hoconFactory)
                }
                is HoconNullableEntry ->{
                    hoconEntry.readConfig(config, hoconFactory)
                }
                is HoconListEntry ->{
                    hoconEntry.readConfig(config, hoconFactory)
                }
                is HoconNestedEntry<*, *> ->{
                    hoconEntry.readConfig(config, hoconFactory)
                }
            }
        }
        entryResolved.forEach {(key, value)->
            val selectedByPropertyName = entries.firstOrNull { it.property?.name ==  key.name}
            if(selectedByPropertyName != null){
                selectedByPropertyName.provideValueCheck(value)
            }else{
                "Property as key $key  not equals to ".output()
                entries.forEach { it.property?.output() }
            }
        }
        return emptyList()
    }
}


inline fun <reified T: HoconResolvable<T>> T.createResolver():HoconResolver<T>{
    return HoconResolver(TypeToken.create<T>())
}

inline fun <T: EventHost, reified C: HoconResolvable<C>> C.createResolver(
    receiver:T,
    noinline block: ResolverBuilder<T, C, String>.() -> Unit
):HoconResolver<C>{
    val builderContainer = ResolverBuilder(receiver, createResolver(), HoconString)
    builderContainer.block()
    return  builderContainer.resolver
}

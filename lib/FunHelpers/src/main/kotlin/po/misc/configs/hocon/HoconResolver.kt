package po.misc.configs.hocon

import com.typesafe.config.Config
import com.typesafe.config.ConfigOrigin
import po.misc.callbacks.common.EventHost
import po.misc.callbacks.event.HostedEvent
import po.misc.configs.hocon.builders.ResolverBuilder
import po.misc.configs.hocon.builders.ResolverEvents
import po.misc.configs.hocon.models.HoconEntry
import po.misc.configs.hocon.models.HoconEntryBase
import po.misc.configs.hocon.models.HoconListEntry
import po.misc.configs.hocon.models.HoconNestedEntry
import po.misc.configs.hocon.models.HoconPrimitives
import po.misc.configs.hocon.models.HoconString
import po.misc.context.component.ComponentID
import po.misc.context.component.componentID
import po.misc.context.component.managedException
import po.misc.context.tracable.TraceableContext
import po.misc.data.helpers.output
import po.misc.data.logging.LogProvider
import po.misc.data.logging.Loggable
import po.misc.data.logging.procedural.ProceduralFlow
import po.misc.data.logging.procedural.ProceduralRecord
import po.misc.data.logging.procedural.StepTolerance
import po.misc.data.logging.processor.logProcessor
import po.misc.functions.Nullable
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

interface HoconResolvable<T: HoconResolvable<T>>: TraceableContext, TokenFactory{
    val resolver: HoconResolver<T>
}

interface HoconConfigurable<T: EventHost, C: HoconResolvable<C>, V: Any> : HoconResolvable<C>{
    val receiver:T
    override val resolver: HoconResolver<C>
    val hoconPrimitive: HoconPrimitives<V>
}


class HoconResolver<C: HoconResolvable<C>>(
    val configToken: TypeToken<C>,
): EventHost, LogProvider<ProceduralRecord> {

    private val parsingSubject: (TypeToken<*>) -> String = { "Parsing ${it.typeName}" }
    override val componentID: ComponentID = componentID().addParamInfo("C", configToken)
    internal val logProcessor = logProcessor {
        ProceduralRecord(it)
    }

    @PublishedApi
    internal val entryResolved: MutableMap<KProperty<*>, HostedEvent<*, *, Unit>> = mutableMapOf()

    internal val memberMap: MutableMap<KClass<out HoconResolvable<*>>, HoconResolvable<*>> = mutableMapOf()

    @PublishedApi
    internal val entryMap: MutableMap<String,  HoconEntryBase<C, *>> = mutableMapOf()

    val events: ResolverEvents<C> = ResolverEvents(this)

    var nowParsing: NowParsing? = null

    private val parsingMessage : (HoconEntryBase<*, *>)-> String = {
        "Parsing ${it.name}"
    }


    init {
        if (events.onStart.event) {
            events.onStart.trigger(Unit)
        }
    }

    private fun readComplete() {
        entryResolved.forEach { (key, value) ->
            val selectedByPropertyName = entryMap[key.name]
            if (selectedByPropertyName != null) {
                selectedByPropertyName.provideValueCheck(value)
            } else {
                "Property as key $key  not equals to ".output()
                entryMap.values.forEach { it.property?.output() }
            }
        }
        if (events.onComplete.event) {
            events.onComplete.trigger(Unit)
        }
    }

    override fun notify(loggable: Loggable) {
        val proceduralLog = ProceduralRecord(loggable)
        logProcessor.logData(proceduralLog)
    }

    private fun identifyConfig(hoconFactory: Config): String{
        nowParsing = NowParsing(hoconFactory.origin())
        return  nowParsing?.parsing?:""
    }

    private fun <C: HoconResolvable<C>> ProceduralFlow<HoconResolver<C>, ProceduralRecord>.processHoconEntry(
        hoconEntry: HoconEntry<C, *>,
        hoconFactory: Config
    ){
        if(hoconEntry.nullable){
            proceduralStep(parsingMessage(hoconEntry), StepTolerance.ALLOW_NULL) {
                hoconEntry.readConfig(hoconFactory, Nullable)
            }
        }else{
            proceduralStep(parsingMessage(hoconEntry)) {
                hoconEntry.readConfig(hoconFactory)
            }
        }
    }

    private fun <C: HoconResolvable<C>> ProceduralFlow<HoconResolver<C>, ProceduralRecord>.processListEntry(
        hoconEntry: HoconListEntry<C, *>,
        hoconFactory: Config
    ){
        if(hoconEntry.nullable) {
            proceduralStep("Parsing ${hoconEntry.name} List", StepTolerance.ALLOW_NULL, StepTolerance.ALLOW_EMPTY_LIST) {
                hoconEntry.readListConfig(hoconFactory, Nullable)
            }
        }else{
            proceduralStep("Parsing ${hoconEntry.name} List", StepTolerance.ALLOW_EMPTY_LIST) {
                hoconEntry.readListConfig(hoconFactory)
            }
        }
    }

    private fun <C: HoconResolvable<C>> ProceduralFlow<HoconResolver<C>, ProceduralRecord>.processNestedEntry(
        hoconEntry: HoconNestedEntry<C, *>,
        hoconFactory: Config
    ){
        if(hoconEntry.nullable) {
            proceduralStep("Switching to ${hoconEntry.name}", StepTolerance.ALLOW_NULL) {
                hoconEntry.readConfig(hoconFactory, Nullable)
            }
        }else{
            proceduralStep("Switching to ${hoconEntry.name}") {
                hoconEntry.readConfig(hoconFactory)
            }
        }
    }

    fun readConfig(hoconFactory: Config) {

        val parsingMessages =  identifyConfig(hoconFactory)
        val info = info(parsingSubject(configToken), "Parsing $parsingMessages")

        logProcessor.logScope(info.toProcedural()) {
            proceduralStep("Parsing Config") {
                for (hoconEntry in entryMap.values) {
                    when (hoconEntry) {
                        is HoconEntry ->  processHoconEntry(hoconEntry, hoconFactory)
                        is HoconListEntry -> processListEntry(hoconEntry, hoconFactory)
                        is HoconNestedEntry<C, *> ->  processNestedEntry(hoconEntry, hoconFactory)
                    }
                }
            }
        }
        readComplete()
    }
    fun registerMember(resolvable: HoconResolvable<*>): HoconResolver<C> {
        memberMap[resolvable::class] = resolvable
        resolvable.resolver.logProcessor.collectData(keepData = false) { notification ->
            logProcessor.logData(notification)
        }
        return this
    }
    fun register(hoconEntry: HoconEntryBase<C, *>): Boolean {
        entryMap[hoconEntry.name.lowercase()] = hoconEntry
        hoconEntry.logProcessor.collectData(keepData = false) { notification ->
            logProcessor.activeRecord?.procedural?.add(notification.toProceduralEntry())
        }
        return true
    }
}

inline fun <reified T: HoconResolvable<T>> T.createResolver():HoconResolver<T>{
    return HoconResolver(TypeToken.create<T>())
}

inline fun <T: EventHost, reified C: HoconResolvable<C>> C.createResolver(
    receiver:T,
    noinline block: ResolverBuilder<T, C, String>.() -> Unit
):HoconResolver<C>{
    val builderContainer = ResolverBuilder(receiver, createResolver(), HoconString.Companion)
    builderContainer.block()
    return  builderContainer.resolver
}

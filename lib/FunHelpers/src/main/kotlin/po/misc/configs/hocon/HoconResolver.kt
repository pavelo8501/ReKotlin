package po.misc.configs.hocon

import com.typesafe.config.Config
import po.misc.callbacks.common.EventHost
import po.misc.callbacks.event.HostedEvent
import po.misc.configs.hocon.builders.ResolverBuilder
import po.misc.configs.hocon.builders.ResolverEvents
import po.misc.configs.hocon.builders.resolver
import po.misc.configs.hocon.models.HoconEntry
import po.misc.configs.hocon.models.HoconEntryBase
import po.misc.configs.hocon.models.HoconListEntry
import po.misc.configs.hocon.models.HoconNestedEntry
import po.misc.configs.hocon.models.HoconPrimitives
import po.misc.configs.hocon.models.HoconString
import po.misc.context.component.ComponentID
import po.misc.context.component.componentID
import po.misc.context.log_provider.LogProvider
import po.misc.context.log_provider.proceduralScope
import po.misc.context.log_provider.withProceduralScope
import po.misc.context.tracable.TraceableContext
import po.misc.data.output.output
import po.misc.data.logging.models.LogMessage
import po.misc.data.logging.procedural.ProceduralFlow
import po.misc.data.logging.procedural.StepTolerance
import po.misc.data.logging.processor.createLogProcessor
import po.misc.data.badges.Badge
import po.misc.data.badges.GenericBadge
import po.misc.data.logging.log_subject.startProcSubject
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
): EventHost, LogProvider {

    override val componentID: ComponentID = componentID().addParamInfo("C", configToken)
    override val logProcessor = createLogProcessor()

    @PublishedApi
    internal val entryResolved: MutableMap<KProperty<*>, HostedEvent<*, *, Unit>> = mutableMapOf()
    internal val memberMap: MutableMap<KClass<out HoconResolvable<*>>, HoconResolvable<*>> = mutableMapOf()
    @PublishedApi
    internal val entryMap: MutableMap<String,  HoconEntryBase<C, *>> = mutableMapOf()

    private val parsingSubject: (TypeToken<*>) -> String = { "Parsing ${it.typeName}" }

    private var nowParsing: NowParsing? = null
    private val parseBadge: GenericBadge = Badge.make("parse")
    private val parsingMessage : (HoconEntryBase<*, *>)-> String = {
        "Parsing ${it.componentName}"
    }

    val events: ResolverEvents<C> = ResolverEvents(this)

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

    override fun notify(logMessage: LogMessage): LogMessage {
        logProcessor.logData(logMessage)
        return logMessage
    }

    private fun identifyConfig(hoconFactory: Config): String{
        nowParsing = NowParsing(hoconFactory.origin())
        return  nowParsing?.parsing?:""
    }

    private fun <C: HoconResolvable<C>> processHoconEntry(hoconEntry: HoconEntry<C, *>, hoconFactory: Config){
        if(hoconEntry.nullable){
            hoconEntry.readConfig(hoconFactory, Nullable)
        }else{
            hoconEntry.readConfig(hoconFactory)
        }
    }

    private fun <C: HoconResolvable<C>> processListEntry(hoconEntry: HoconListEntry<C, *>, hoconFactory: Config){
        if(hoconEntry.nullable) {
            hoconEntry.readListConfig(hoconFactory, Nullable)
        }else{
            hoconEntry.readListConfig(hoconFactory)
        }
    }

    private fun <C: HoconResolvable<C>> processNestedEntry(
        flow: ProceduralFlow<HoconResolver<C>>,
        entry: HoconNestedEntry<C, *>,
        hoconFactory: Config
    ){
        val tolerance: StepTolerance = if(entry.nullable) StepTolerance.ALLOW_NULL else StepTolerance.STRICT
        with(flow){
            step("Parsing ${entry.componentName}", parseBadge, tolerance) {
                entry.readConfig(hoconFactory, Nullable)
            }
        }
    }

    fun readConfig(hoconFactory: Config) {
        val msg = infoMsg(startProcSubject(::readConfig), "Parsing ${configInfo(hoconFactory).parsing}")
        withProceduralScope(msg){
            step("Parsing Config", parseBadge) {
                for (hoconEntry in entryMap.values) {
                    when (hoconEntry) {
                        is HoconEntry ->  processHoconEntry(hoconEntry, hoconFactory)
                        is HoconListEntry -> processListEntry(hoconEntry, hoconFactory)
                        is HoconNestedEntry<C, *> ->  processNestedEntry(this@withProceduralScope,  hoconEntry, hoconFactory)
                    }
                }
            }
        }
        readComplete()
    }

    fun registerMember(resolvable: HoconResolvable<*>): HoconResolver<C> {
        memberMap[resolvable::class] = resolvable
        resolvable.resolver.logProcessor.collectData(keepData = false) { notification ->
            logProcessor.log(notification)
        }
        return this
    }
    fun register(hoconEntry: HoconEntryBase<C, *>): Boolean {
        entryMap[hoconEntry.componentName.lowercase()] = hoconEntry
        return true
    }

    companion object {
        fun configInfo(hoconFactory: Config): NowParsing{
           return NowParsing(hoconFactory.origin())
        }
    }
}

@Deprecated("Change to resolver()")
inline fun <T: EventHost, reified C: HoconResolvable<C>> C.createResolver(
    receiver:T,
    noinline block: ResolverBuilder<T, C, String>.() -> Unit
):HoconResolver<C>{
    val builderContainer = ResolverBuilder(receiver, resolver(), HoconString.Companion)
    builderContainer.block()
    return  builderContainer.resolver
}

@Deprecated("Change to resolver()")
inline fun <reified T: HoconResolvable<T>> T.createResolver():HoconResolver<T> = resolver()


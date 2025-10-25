package po.misc.configs.hocon.models

import com.typesafe.config.Config
import com.typesafe.config.ConfigValueType
import po.misc.callbacks.event.HostedEvent
import po.misc.configs.hocon.HoconResolvable
import po.misc.configs.hocon.HoconResolver
import po.misc.configs.hocon.extensions.applyConfig
import po.misc.configs.hocon.extensions.parseListValue
import po.misc.configs.hocon.extensions.parseValue
import po.misc.context.component.Component
import po.misc.context.component.ComponentID
import po.misc.context.component.componentID
import po.misc.context.component.managedException
import po.misc.data.logging.Loggable
import po.misc.data.logging.models.Notification
import po.misc.data.logging.processor.logProcessor
import po.misc.functions.Nullable
import po.misc.types.safeBaseCast
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty


sealed class HoconEntryBase<T: HoconResolvable<T>, V>(
    val receiver: T,
    val hoconPrimitive: HoconPrimitives<V>,
): Component {

    var property: KProperty<*>? = null
        protected set

    val name: String get() =  property?.name?:"Undefined"

    abstract override val componentID: ComponentID

    protected val resolver: HoconResolver<T> get() = receiver.resolver
    val receiverType: TypeToken<T> get() = receiver.resolver.configToken
    val valueTypeToken: TypeToken<V> get() = hoconPrimitive.typeToken
    val nullabilityFromType: Boolean get() = valueTypeToken.isNullable
    open  val nullable: Boolean get() = property?.returnType?.isMarkedNullable?: nullabilityFromType

    protected val subjectParsing: String get() = "Parsing $name"
    protected val criticalFailure: String get() = "Critical failure"

    protected val missingValueError: String get() =  "Property $name is non nullable. Value is missing in config file. Check config."
    protected val wrongTypeError : (ConfigValueType)-> String = {
        "Property $name is expected to be of type ${valueTypeToken.typeName}. Got $it"
    }
    protected val valueAccessError: String get() =  "Property:  ${componentID.componentName} is accessing result value before  assignment"

    var resultBacking:V? = null
        protected set

    val result:V get() = resultBacking ?:run {
        if (nullable) {
            @Suppress("Unchecked_cast")
            resultBacking as V
        } else {
            warn(criticalFailure, valueAccessError)
            throw managedException(valueAccessError)
        }
    }

    internal val logProcessor = logProcessor()
    internal var onResultAvailable: ((V) -> Unit) ? = null
    internal fun resultAvailable(callback: (V) -> Unit){
        if(onResultAvailable != null){
            warn("Result provider", "OnValueAvailable being overwritten")
        }
        onResultAvailable = callback
    }
    protected open fun registerResult(parsedResult:V):V{
        resultBacking = parsedResult
        onResultAvailable?.invoke(parsedResult)
        return result
    }

    internal open fun initialize(kProperty: KProperty<*>){
        property = kProperty
        resolver.register(this)
    }

    fun checkType(valueType: ConfigValueType): Boolean{
       return if (valueType != hoconPrimitive.hoconType) {
            warn(subjectParsing, "$name expected ${hoconPrimitive.hoconType} but found $valueType")
            false
        }else{
            true
        }
    }

   open  fun readConfig(config: Config):V{
       return if(config.hasPath(name)){
            val rawValue = config.getValue(name)
            val valueType = rawValue.valueType()
            val typeChecked = checkType(valueType)
            if(!typeChecked){
                warn(subjectParsing, wrongTypeError(valueType))
                throw managedException(wrongTypeError(valueType))
            }
            val parsedValue =  parseValue(rawValue)
            registerResult(parsedValue)
        }else{
            warn(subjectParsing, missingValueError)
            throw managedException(missingValueError)
        }
    }

    open fun readConfig(config: Config, resultType: Nullable):V?{
       return if(config.hasPath(name)){
            val rawValue = config.getValue(name)
            val typeChecked = checkType(rawValue.valueType())
            if(!typeChecked){
                return null
            }
           val parsedValue =  parseValue(rawValue)
           registerResult(parsedValue)
        }else{
            null
        }
    }
    fun provideValueCheck(event: HostedEvent<*, *, Unit>){
        event.safeBaseCast<HostedEvent<*, V & Any, Unit>>()?.let {
            val thisValue = resultBacking
            if(thisValue != null){
               it.triggerValidating(thisValue)
            }else{
                null
            }
        }
    }
    override fun notify(loggable: Loggable) {
        val notification = Notification(loggable)
        logProcessor.logData(notification)
    }
    override fun toString(): String = componentID.componentName
}

class HoconEntry<T: HoconResolvable<T>, V>(
    receiver: T,
    hoconPrimitive: HoconPrimitives<V>,
): HoconEntryBase<T, V>(receiver, hoconPrimitive) {

    override val componentID: ComponentID = componentID({name}).addParamInfo("T", receiverType).addParamInfo("V", valueTypeToken)
}

class HoconListEntry<T: HoconResolvable<T>, V>(
    receiver: T,
    val hoconList: HoconList<V, *>,
    val hoconEntry:  HoconEntry<T, V>
): HoconEntryBase<T, V>(receiver, hoconList) {

    var listValue: List<V> = emptyList()
    override val componentID: ComponentID = componentID({name}).addParamInfo("T", receiverType).addParamInfo("V", valueTypeToken)

   // override val nullable: Boolean get() = hoconList.listTypeToken.isNullable

    fun registerListResult(parsedResult: List<V>): List<V>{
        listValue =parsedResult
        return parsedResult
    }

    fun readListConfig(config: Config, nullable: Nullable): List<V>?{
       return if(config.hasPath(name)) {
            val rawValue = config.getValue(name)
            val valueType = rawValue.valueType()
            val typeChecked = checkType(valueType)
            if (!typeChecked) {
                return null
            }
            val parsedValue = parseListValue(rawValue)
            registerListResult(parsedValue)
        }else{
            null
        }
    }

    fun readListConfig(config: Config): List<V> {
        return if (config.hasPath(name)) {
            val rawValue = config.getValue(name)
            val valueType = rawValue.valueType()
            val typeChecked = checkType(valueType)
            if (!typeChecked) {
                warn(subjectParsing, wrongTypeError(valueType))
                throw managedException(wrongTypeError(valueType))
            }
            val parsedValue = parseListValue(rawValue)
            registerListResult(parsedValue)
        } else {
            warn(subjectParsing, missingValueError)
            throw managedException(missingValueError)
        }
    }
}

class HoconNestedEntry<T: HoconResolvable<T>, V: HoconResolvable<V>>(
    receiver: T,
    hoconPrimitive: HoconObject<V>,
    val nestedClass: V,
): HoconEntryBase<T, V>(receiver, hoconPrimitive) {

    override val componentID: ComponentID =
        componentID({ name }).addParamInfo("T", receiverType).addParamInfo("V", valueTypeToken)

    private val sourceNotProvidedError = "Source not provided.  Property not marked nullable but"

    override fun initialize(kProperty: KProperty<*>) {
        property = kProperty
        resolver.registerMember(nestedClass)
        resolver.register(this)
    }

    override fun readConfig(config: Config): V {
        if (!config.hasPath(name)) {
            warn(subjectParsing, missingValueError)
            throw managedException(missingValueError)
        }
        val rawValue = config.getValue(name)
        val valueType = rawValue.valueType()
        val typeChecked = checkType(valueType)
        if (!typeChecked) {
            warn(subjectParsing, wrongTypeError(valueType))
            throw managedException(wrongTypeError(valueType))
        }
        val config = config.getConfig(name)
        nestedClass.applyConfig(config)
        return  nestedClass
    }

    override fun readConfig(config: Config, resultType: Nullable): V? {
        if(!config.hasPath(name)){
            return null
        }
        val rawValue = config.getValue(name)
        val valueType = rawValue.valueType()
        val typeChecked = checkType(valueType)
        if (!typeChecked) {
            return null
        }
        val config = config.getConfig(name)
        nestedClass.applyConfig(config)
        return nestedClass
    }
}
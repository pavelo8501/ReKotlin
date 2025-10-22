package po.misc.configs.hocon.models

import com.typesafe.config.Config
import com.typesafe.config.ConfigValueType
import po.misc.callbacks.event.HostedEvent
import po.misc.configs.hocon.HoconResolvable
import po.misc.configs.hocon.HoconResolver
import po.misc.configs.hocon.extensions.applyConfig
import po.misc.configs.hocon.extensions.parseList
import po.misc.configs.hocon.extensions.parseValue
import po.misc.context.component.Component
import po.misc.context.component.managedException
import po.misc.types.safeBaseCast
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty


sealed class HoconEntryBase<T: HoconResolvable<T>, V: Any>(
    val receiver: T,
    val hoconPrimitive: HoconPrimitives<V>
): Component {

    var property: KProperty<*>? = null

    val name: String get() =  property?.name?:"Undefined"
    abstract val componentName: String

    //internal var resolver: HoconConfigResolver<T>? = null
    protected val resolver: HoconResolver<T> get() = receiver.resolver
    val receiverType: TypeToken<T> get() = receiver.resolver.typeToken
    val valueTypeToken: TypeToken<V> get() = hoconPrimitive.typeToken

    var value:V? = null
        internal set

    protected val valueTypeName: String get() = valueTypeToken.typeName
    protected val formatMessage : (HoconEntryBase<*, *>, String) -> String = {entry, text->
        "[${entry.componentName}] -> $text"
    }

    internal var onValueAvailable: ((V) -> Unit) ? = null

    internal fun valueAvailable(callback: (V) -> Unit){
        if(onValueAvailable != null){
            warn("Result provider", "OnValueAvailable being overwritten")
        }
        onValueAvailable = callback
    }
    abstract fun readConfig(configModel: T, config: Config):V?

    protected fun checkRawValue(configModel: T, config: Config, required: Boolean):V?{
        val found = config.hasPath(name)
        if (!found && required) {
            val msg = formatMessage(this, "$name is required but missing")
            throw managedException(msg)
        }
        if(!found){
            return null
        }
        val rawValue = config.getValue(name)
        if (rawValue.valueType() != hoconPrimitive.hoconType) {
            throw managedException("$name expected ${hoconPrimitive.hoconType} but found ${rawValue.valueType()}")
        }
        val parsedValue = config.parseValue<V>(name, hoconPrimitive)
        val msg = "Value $parsedValue<${hoconPrimitive.hoconType}> for entry: $name parse. Success"
        info("Value parsed", msg)
        value = parsedValue
        onValueAvailable?.invoke(parsedValue)
        return  parsedValue
    }

    fun provideValueCheck(event: HostedEvent<*, *, Unit>){
        event.safeBaseCast<HostedEvent<*, V, Unit>>()?.let {
            val thisValue = value
            if(thisValue != null){
               it.triggerValidating(thisValue)
            }else{
                null
            }
        }
    }

    fun initialize(delegateProperty: KProperty<*>){
        property = delegateProperty
        resolver.register(this)
    }

    override fun toString(): String = componentName
}

class HoconEntry<T: HoconResolvable<T>, V: Any>(
    receiver: T,
    hoconPrimitive: HoconPrimitives<V>,
    val mandatory: Boolean
): HoconEntryBase<T, V>(receiver, hoconPrimitive) {

    override val componentName: String get() =  "HoconEntry<${receiverType.typeName}, $valueTypeName>[${name}]"

    override fun readConfig(configModel: T, config: Config):V? {
       return checkRawValue(configModel, config, mandatory)
    }
}

class HoconNullableEntry<T: HoconResolvable<T>, V: Any>(
    receiver: T,
    hoconPrimitive: HoconPrimitives<V>,
): HoconEntryBase<T, V>(receiver,  hoconPrimitive) {
    val hoconNullable = HoconNullable

    override val componentName: String = "HoconNullableEntry<${receiverType.typeName}, ${valueTypeName}>[${name}]"

    override fun readConfig(configModel: T, config: Config):V? {
       return checkRawValue(configModel, config, false)
    }
}

class HoconNestedEntry<T: HoconResolvable<T>, V: HoconResolvable<V>>(
    receiver: T,
    hoconPrimitive: HoconPrimitives<Any>,
    val nestedClass: V,
): HoconEntryBase<T, Any>(receiver, hoconPrimitive) {
    override val componentName: String = "HoconNestedEntry<${receiverType.typeName}, ${typeToken.typeName}>[${name}]"
    val nestedResolver: HoconResolver<V> get() = nestedClass.resolver
    val typeToken: TypeToken<V> get() = nestedClass.resolver.typeToken

    init {
        resolver.registerMember(nestedClass)
    }

    private  fun forwardConfig(config: Config) {
        nestedClass.applyConfig(config)
    }
    override fun readConfig(configModel: T, config: Config) {
        if (!config.hasPath(name)) {
            val errMsg = formatMessage(this, "$name is required but missing")
            throw managedException(errMsg)
        }
        val nestedConfig =  config.getConfig(name)
        forwardConfig(nestedConfig)
    }
}

class HoconListEntry<T: HoconResolvable<T>, V: Any>(
    receiver: T,
    hoconPrimitive: HoconPrimitives<V>,
): HoconEntryBase<T, V >(receiver, hoconPrimitive) {

    override val componentName: String = "HoconListEntry<${receiverType.typeName}, ${valueTypeName}>[${name}]"
    var listValue: List<V> = emptyList()

    override fun readConfig(configModel: T, config: Config):V?{
        val found = config.hasPath(name)
        if (!found) {
            val msg = formatMessage(this, "$name is required but missing")
            throw managedException(msg)
        }
        val rawValue = config.getValue(name)

        if(rawValue.valueType() == ConfigValueType.LIST){
            listValue = config.parseList(name, hoconPrimitive)
            val msg = "Value List<${hoconPrimitive.hoconType}> for entry: $name  ${listValue.size} records parsed. Success"
            info("Parsed", msg)
            listValue
        }else{
            throw managedException("$name expected ${hoconPrimitive.hoconType} but found ${rawValue.valueType()}")
        }
        return null
    }
}


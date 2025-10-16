package po.misc.configs.hocon

import com.typesafe.config.Config
import com.typesafe.config.ConfigValueType
import po.misc.context.Component
import po.misc.context.managedException
import po.misc.data.logging.Verbosity
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty


sealed class HoconEntryBase<T: Any, V: Any>(
    val resolver: HoconConfigResolver<T>,
    val property: KProperty<*>,
    val hoconPrimitive:  HoconPrimitives<V>
): Component {

    override var verbosity: Verbosity = Verbosity.Info
    abstract override val componentName: String

    //internal var resolver: HoconConfigResolver<T>? = null
    val receiverType: TypeToken<T> get() = resolver.typeToken
    val valueTypeToken: TypeToken<V> get() = hoconPrimitive.typeToken

    var value:V? = null
        internal set

    val name: String = property.name

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
    abstract fun readConfig(configModel: T, config: Config)
    protected fun checkRawValue(configModel: T, config: Config, required: Boolean):V?{
         val found = config.hasPath(name)
        if (!found && required) {
            val msg =formatMessage(this, "$name is required but missing")
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

    fun registerInResolver(entry: HoconEntryBase<T, V>){
        resolver.register(entry)
    }

    override fun toString(): String = componentName
}

class HoconEntry<T: Any, V: Any>(
    resolver: HoconConfigResolver<T>,
    property: KProperty<*>,
    hoconPrimitive:  HoconPrimitives<V>,
    val mandatory: Boolean
): HoconEntryBase<T, V>(resolver, property, hoconPrimitive) {
    override val componentName: String get() =  "HoconNestedEntry<${receiverType.typeName}, $valueTypeName>[${property.name}]"
    init {
        registerInResolver(this)
    }
    override fun readConfig(configModel: T, config: Config) {
        checkRawValue(configModel, config, mandatory)
    }
}

class HoconNullableEntry<T: Any, V: Any>(
    resolver: HoconConfigResolver<T>,
    property: KProperty<*>,
    hoconPrimitive:  HoconPrimitives<V>,
): HoconEntryBase<T, V>(resolver, property, hoconPrimitive) {
    val hoconNullable = HoconNullable
    override val componentName: String = "HoconNullableEntry<${receiverType.typeName}, ${valueTypeName}>[${property.name}]"
    init {
        registerInResolver(this)
    }
    override fun readConfig(configModel: T, config: Config) {
        checkRawValue(configModel, config, false)
    }
}

class HoconNestedEntry<T: Any, V: HoconResolvable<V>>(
    resolver: HoconConfigResolver<T>,
    property: KProperty<*>,
    hoconPrimitive: HoconPrimitives<Any>,
    val nestedClass: V,
): HoconEntryBase<T, Any>(resolver, property, hoconPrimitive) {
    override val componentName: String = "HoconNestedEntry<${receiverType.typeName}, ${typeToken.typeName}>[${property.name}]"
    val nestedResolver: HoconConfigResolver<V> get() = nestedClass.resolver
    val typeToken: TypeToken<V> get() = nestedClass.resolver.typeToken

    init {
        resolver.register(this)
        resolver.registerMember(nestedClass)
    }

    private  fun forwardConfig(config: Config) {
        nestedClass.readConfig(config)
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

class HoconListEntry<T: Any, V: Any>(
    resolver: HoconConfigResolver<T>,
    property: KProperty<*>,
    hoconPrimitive:  HoconPrimitives<V>,
): HoconEntryBase<T, V>(resolver, property, hoconPrimitive) {

    override val componentName: String = "HoconListEntry<${receiverType.typeName}, ${valueTypeName}>[${property.name}]"

    var listValue: List<V> = emptyList()


    init {
        registerInResolver(this)
    }

    override fun readConfig(configModel: T, config: Config){
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
        }else{
            throw managedException("$name expected ${hoconPrimitive.hoconType} but found ${rawValue.valueType()}")
        }
    }
}


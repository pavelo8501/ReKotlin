package po.misc.reflection.objects

import po.misc.collections.StaticTypeKey
import po.misc.reflection.objects.components.KSurrogate
import po.misc.data.helpers.textIfNotNull
import po.misc.data.styles.SpecialChars
import po.misc.exceptions.throwManaged
import po.misc.reflection.objects.components.ManagerHooks
import po.misc.reflection.objects.components.ObjectsMap
import po.misc.reflection.properties.PropertyContainer
import po.misc.reflection.properties.PropertyIOBase
import po.misc.types.castOrManaged
import po.misc.types.getOrManaged
import java.util.EnumMap
import kotlin.reflect.KClass


class ObjectManager<E: Enum<E>, S: Composed>(
    val objectName : String,
    val sourceKey: E,
    val keyClass: Class<E>
): PropertyContainer<S> {

    enum class Status{
        New,
        PropertyInitialized,
        Initialized,
        InitFailure
    }

    val hooks: ManagerHooks = ManagerHooks()
    var status : Status = Status.New

    @PublishedApi
    internal var dataClazz: KClass<S>? = null

    override val containerName: String get() = objectName
    override val hasSource: Boolean get() = sourceBacking !=null

    override val dataSimpleName: String get() = dataClazz.textIfNotNull<KClass<S>>("N/A"){simpleName.toString()}
    override val dataQualifiedName: String get() = dataClazz.textIfNotNull<KClass<S>>("N/A"){qualifiedName.toString()}

    private var sourceBacking :  KSurrogate<E, S>? = null
    var sourceClass :  KSurrogate<E, S>
        set(value) { sourceBacking = value }
        get() = sourceBacking.getOrManaged("objectName KSurrogate")

    val sourceObject:S get() = sourceClass.source
    private  val temporaryPropertyMap : MutableMap<StaticTypeKey<*>, PropertyIOBase<* , *>> = mutableMapOf()
    val auxSurrogateMap : EnumMap<E, KSurrogate<E, *>> = EnumMap(keyClass)

    fun storeProperty(property: PropertyIOBase<*, *>){
        temporaryPropertyMap[property.typeKey] = property
    }

    fun setSource(source:S){
        if(sourceBacking == null){
            sourceClass = KSurrogate(sourceKey, source, hooks)
            status = Status.Initialized
        }
    }

    fun <T: Composed> addSurrogate(key:E, surrogate:KSurrogate<E, T>){
        temporaryPropertyMap[surrogate.typeKey]?.let { property->
            surrogate.addProperty<T>(property.castOrManaged())
        }
        hooks.onNewMap?.invoke(surrogate)
        auxSurrogateMap[key] = surrogate
    }

    fun dataInfo(): String{
        val propertyInfoList = mutableListOf<String>()
        auxSurrogateMap.values.forEach {
            propertyInfoList.add(it.toString())
        }
        return propertyInfoList.joinToString(SpecialChars.NewLine.char)
    }
    fun getValue(propertyName: String): Any{
       return sourceClass[propertyName]?.readCurrentValue()?:run {
           throwManaged("kk")
       }
    }
}
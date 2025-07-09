package po.misc.reflection.properties

import po.misc.collections.SlidingBuffer
import po.misc.collections.StaticTypeKey
import po.misc.data.delegates.ComposableProperty
import po.misc.data.helpers.textIfNull
import po.misc.exceptions.ManagedCallSitePayload
import po.misc.interfaces.IdentifiableContext
import po.misc.reflection.objects.Composed
import po.misc.reflection.properties.models.PropertyUpdate
import po.misc.types.castOrManaged
import po.misc.types.getOrManaged
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1


sealed class PropertyIOBase<T: Any, V: Any>(
    val propertyInfo: PropertyInfo<T, V>,
    val propertyType: PropertyType,
    private var currentValue: V?
): Composed, IdentifiableContext {

    enum class PropertyType {
        Computed,
        StaticallySet,
        DelegateProvided
    }

    enum class PropertyIOType(val value: String) {
        ReadOnly("val"),
        RW("var")
    }

    override val contextName: String get() = "PropertyIOBase[$propertyName]"

    val ioType: PropertyIOType get(){
       return if(propertyInfo.mutable){
            PropertyIOType.RW
        }else{
            PropertyIOType.ReadOnly
        }
    }
    val typeKey: StaticTypeKey<T> = propertyInfo.typeKey
    val resultTypeKey: StaticTypeKey<V>? = propertyInfo.returnTypeKey
    val propertyName: String = propertyInfo.propertyName

    protected var receiverBacking: T? = null
    var receiver: T
        get() = receiverBacking.getOrManaged("receiver @ PropertyIO")
        set(value) {
            if (receiverBacking == null) {
                receiverBacking = value
            }
        }

    val buffer: SlidingBuffer<PropertyUpdate<V>, V> = SlidingBuffer(5) {
        PropertyUpdate(propertyName, it)
    }

    init {
        currentValue?.let { buffer.add(it) }
    }

    protected val asKMutableProperty: KMutableProperty1<T, V> by lazy {
        propertyInfo.property.castOrManaged()
    }
    protected val asKProperty: KProperty1<T, V> by lazy {
        propertyInfo.property.castOrManaged()
    }

    private fun payload(message: String): ManagedCallSitePayload{
      return  ManagedCallSitePayload.create(message, this)
    }

    fun initialize(dataObject:T){
        receiver = dataObject
        buffer.add(asKProperty.get(dataObject))
    }

    fun provideReceiver(receiver: T) {
        receiverBacking = receiver
    }

    fun extractValue(receiver: T): V {
        return asKProperty.get(receiver)
    }
    fun setValue(value: V) {
        buffer.addIfDifferent(value) {
            if (ioType == PropertyIOType.RW) {
                asKMutableProperty.set(receiver, it)
            }
        }
    }

    fun getValue(): V {
        return  buffer.getValue()?:run {
           val type =  propertyInfo.valueTypeData.getOrManaged(payload("Default result unavailable. No valueTypeData"))
           getDefaultForType(type).getOrManaged(payload("No default result for type ${type.kType}") )
        }
    }

    fun readCurrentValue(): V {
        return currentValue.getOrManaged("PropertyIO")
    }

    fun updateHistory(): List<PropertyUpdate<V>>{
       return buffer.toList()
    }

    fun <R: Any>  returnIfReceiver(receiver: R):PropertyIOBase<T, V>?{
     return  if(propertyInfo.typeKey.isInstanceOfType(receiver)){
           return this
       }else{
           null
       }
    }
}

class SourcePropertyIO<T: Any, V: Any>(
    propertyInfo: PropertyInfo<T, V>,
    propertyType: PropertyType,
):PropertyIOBase<T, V>(propertyInfo, propertyType, null){

    internal val auxDataProperty: MutableMap<StaticTypeKey<*>, PropertyIO<*, V>> = mutableMapOf()

    val propertySlots: PropertyGroup<Any, V> = PropertyGroup()

    override fun toString(): String{
        val typeName : String = propertyInfo.valueTypeData?.typeName.textIfNull("Unit")
        val result = "${ioType.value} $propertyName :$typeName get() = ${getValue()}"
        return result
    }

}


class PropertyIO<T: Any, V: Any>(
    propertyInfo: PropertyInfo<T, V>,
    propertyType: PropertyType,
    currentValue: V?
):PropertyIOBase<T,V>(propertyInfo, propertyType, currentValue) {

    internal var onValueChanged:(T.(V)-> Unit)? = null

    private fun updateValue(value:V){
        asKMutableProperty.set(receiver, value)
        onValueChanged?.invoke(receiver, value)
    }

    override fun toString(): String{
       return "Property[${propertyName}](Value : ${getValue()}"
    }
}

fun <E: Enum<E>, T, V: Any> ComposableProperty<T, V>.createPropertyIO(
    receiver:T,
    property: KProperty1<T, V>

):SourcePropertyIO<T, V> where T : Composed, T: IdentifiableContext{
    val propertyInfo = property.toPropertyInfo(receiver::class as KClass<T>)
    val property = SourcePropertyIO(propertyInfo, PropertyIOBase.PropertyType.DelegateProvided)
    property.provideReceiver(receiver)
    return property
}

fun <T: IdentifiableContext,  V: Any> createSourcePropertyIO(
    receiver:T,
    property: KProperty1<T, V>,
    valueClass: KClass<V>,
):SourcePropertyIO<T, V>{

    val propertyInfo =  when(property){
        is KMutableProperty1-> property.toPropertyInfo(receiver::class as KClass<T>)
        else -> property.toPropertyInfo(receiver::class as KClass<T>)
    }
    propertyInfo.setValueClass(valueClass)
    val propertyIO = SourcePropertyIO(propertyInfo, PropertyIOBase.PropertyType.Computed)
    propertyIO.receiver = receiver
    return propertyIO
}


fun <T: Any, V: Any> createPropertyIO(
    property: KProperty1<T, V>,
    receiver:T,
    initialValue:V? = null
):PropertyIO<T, V>{

    val propertyInfo =  when(property){
        is KMutableProperty1->{
            property.toPropertyInfo(receiver::class as KClass<T>)
        }else -> {
            property.toPropertyInfo(receiver::class as KClass<T>)
        }
    }
    initialValue?.let {
        val valueClass = it::class as KClass<V>
        propertyInfo.setValueClass(valueClass)
    }
    val propertyIO = PropertyIO(propertyInfo, PropertyIOBase.PropertyType.Computed, initialValue)
    propertyIO.receiver = receiver
    return propertyIO
}



fun <T: Any,  V: Any> createPropertyIO(
    property: KProperty1<T, V>,
    clazz: KClass<T>,
    initialValue:V? = null
):PropertyIO<T, V>{

    val propertyInfo =  when(property){
        is KMutableProperty1->{
             property.toPropertyInfo(clazz)
        }else -> {
             property.toPropertyInfo(clazz)
        }
    }
    initialValue?.let {
        val valueClass = it::class as KClass<V>
        propertyInfo.setValueClass(valueClass)
    }
    return PropertyIO(propertyInfo, PropertyIOBase.PropertyType.Computed, initialValue)
}
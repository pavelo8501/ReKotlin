package po.misc.reflection.properties

import po.misc.collections.StaticTypeKey
import po.misc.data.delegates.ComposableProperty
import po.misc.data.helpers.makeIndention
import po.misc.data.styles.SpecialChars
import po.misc.reflection.objects.Composed
import po.misc.types.castOrManaged
import po.misc.types.getOrManaged
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

interface DataBuffer{
   val currentValue: Any?
   val bufferedValue: Any?
   val isDifferent: Boolean
}

sealed class PropertyIOBase<T: Composed, V: Any>(
    protected  val propertyInfo: PropertyInfo<T, V>,
    protected  val valueClass: KClass<V>,
    val propertyType: PropertyType,
    var currentValue: V
){
    enum class PropertyType{
        Computed,
        StaticallySet,
        DelegateProvided
    }
    enum class PropertyIOType(val value: String){
        ReadOnly("val"),
        RW("var")
    }
    val ioType: PropertyIOType = PropertyIOType.RW
    val typeKey: StaticTypeKey<T> = propertyInfo.typeKey
    val resultTypeKey: StaticTypeKey<V>? = propertyInfo.returnTypeKey
    val propertyName: String = propertyInfo.propertyName

    protected var receiverBacking:T? = null
    var receiver:T
        get() = receiverBacking.getOrManaged("receiver @ PropertyIO ${valueClass.simpleName}")
        set(value) { if(receiverBacking == null){ receiverBacking =value } }

    var bufferedValue: V? = null
    val isDifferent: Boolean  get() = currentValue != bufferedValue
    val isBufferNull: Boolean get() = bufferedValue == null

    protected val propertyStr: String
        get() = "Object(${propertyInfo.clazz.simpleName}) ${ioType.value} ${propertyName}:${resultTypeKey?.typeName} = $currentValue ${SpecialChars.NewLine}"

    abstract fun initialize(dataObject:T)
    abstract fun writeValue(dataObject:T, value:V)
    abstract fun setValue(value:V)
    abstract fun flushBuffer()

    protected  val asKMutableProperty: KMutableProperty1<T, V> by lazy {
        propertyInfo.property.castOrManaged()
    }
    protected val asKProperty: KProperty1<T,V> by lazy {
        propertyInfo.property.castOrManaged()
    }

    fun provideReceiver(receiver: T){
        receiverBacking = receiver
    }

    fun getValue():V{
        return currentValue
    }
    fun  readCurrentValue():V{
        return  currentValue.getOrManaged("PropertyIO")
    }
}

class SourcePropertyIO<T: Composed, V: Any>(
    propertyInfo: PropertyInfo<T, V>,
    valueClass: KClass<V>,
    propertyType: PropertyType,
    currentValue: V
):PropertyIOBase<T,V>(propertyInfo, valueClass, propertyType, currentValue), DataBuffer {

   internal val auxDataProperty: MutableMap<StaticTypeKey<*>, PropertyIO<*, V>> = mutableMapOf()

    private fun setValueInternal(value:V){
        currentValue = value
        bufferedValue = value
    }

    private fun onAuxPropertyChange(receiver: Composed, value:V): Unit{
        if(value != currentValue){
            setValueInternal(value)
        }
    }

    fun attachAuxDataProperty(property:  PropertyIO<*, V>){
        println("$property attached to -> ${this}")
        property.onValueChanged = ::onAuxPropertyChange
        auxDataProperty[property.typeKey] = property
    }

    override fun toString(): String{
        var propertiesStr = "SourceProperty[${propertyName}](Value : ${currentValue}, Buffered: ${bufferedValue})${SpecialChars.NewLine}"
        propertiesStr += "AuxDataProperties[${auxDataProperty.size}]"
       val formattedChild =  auxDataProperty.values.joinToString(
            separator = SpecialChars.NewLine.toString(),
            prefix = makeIndention("",4,"-") )
        propertiesStr += formattedChild
        return propertiesStr
    }

    override fun initialize(dataObject: T) {
        receiver = dataObject
        bufferedValue = asKProperty.get(dataObject)
        bufferedValue?.let {
            currentValue = it
        }
    }

    override fun writeValue(dataObject: T, value: V) {

    }

    override fun setValue(value:V){
        bufferedValue = value
        currentValue = value
    }

    override fun flushBuffer(){
        bufferedValue?.let {
            currentValue = it
        }
    }

}

class PropertyIO<T: Composed, V: Any>(
    propertyInfo: PropertyInfo<T, V>,
    valueClass: KClass<V>,
    propertyType: PropertyType,
    currentValue: V
):PropertyIOBase<T,V>(propertyInfo, valueClass, propertyType, currentValue) {

    internal var onValueChanged:(T.(V)-> Unit)? = null

    private fun updateValue(value:V){
        asKMutableProperty.set(receiver, value)
        currentValue = value
        onValueChanged?.invoke(receiver, value)
    }

    override fun initialize(dataObject:T){
        receiver = dataObject
        bufferedValue = asKProperty.get(dataObject)

    }

    override fun writeValue(dataObject:T, value:V){
        if(ioType == PropertyIOType.RW){
            if(currentValue != value){
                updateValue(value)
            }
        }
    }
    override fun setValue(value:V){
        currentValue = value
        bufferedValue = value
        if(ioType == PropertyIOType.RW){
            asKMutableProperty.set(receiver, value)
        }
    }

    override fun flushBuffer(){
        bufferedValue?.let {
            currentValue = it
            updateValue(it)
        }
    }

    override fun toString(): String{
       return "Property[${propertyName}](Value : ${currentValue}, Buffered: ${bufferedValue})"
    }
}

fun <E: Enum<E>, T: Composed, V: Any>  ComposableProperty<T, V, E>.createPropertyIO(
    receiver:T,
    property: KProperty1<T, V>,
    clazz: KClass<V>,
    value:V
):SourcePropertyIO<T, V>{
    val property = SourcePropertyIO(property.toPropertyInfo(receiver::class as KClass<T>), clazz, PropertyIOBase.PropertyType.DelegateProvided, value)
    property.provideReceiver(receiver)
    return property
}


fun <T: Composed,  V: Any> KProperty1<T, V>.createPropertyIO(
    clazz: KClass<T>,
    resultClazz: KClass<V>,
    value:V
):PropertyIO<T, V>{
    val property = this.toPropertyInfo(clazz)
    property.returnTypeKey = StaticTypeKey.createTypeKey(resultClazz)
    return PropertyIO(property, resultClazz, PropertyIOBase.PropertyType.Computed, value)
}

fun <T: Composed,  V: Any> PropertyInfo<T, V>.createPropertyIO(
    clazz: KClass<V>,
    type: PropertyIOBase.PropertyType,
    initialValue:V
):PropertyIO<T, V>{
   return initialValue.let {
       this.returnTypeKey = StaticTypeKey.createTypeKey(it::class as KClass<V>)
       PropertyIO(this, clazz, type, it)
   }
}
package po.misc.reflection.classes

import po.misc.callbacks.CallbackManager
import po.misc.callbacks.builders.callbackManager
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.subIdentity
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.reflection.properties.PropertyGroup
import po.misc.reflection.properties.SourcePropertyIO
import po.misc.types.TypeData
import po.misc.types.castOrManaged


interface WithSurrogateHooks<T: CTX>{
    fun groupRegistered(hook: (PropertyGroup<T, *>)-> Unit)
}

class SurrogateHooks<T: CTX>(): WithSurrogateHooks<T>{
    internal var onGroupRegistered: ((PropertyGroup<T, *>)-> Unit)? = null
    override fun groupRegistered(hook: (PropertyGroup<T, *>)-> Unit){
        onGroupRegistered = hook
    }
}

class KSurrogate<T: CTX>(
    val receiver:T,
    val hooks:SurrogateHooks<T> = SurrogateHooks()
):AbstractMutableMap<String, SourcePropertyIO<T, Any>>(), WithSurrogateHooks<T> by hooks, CTX {

    enum class SurrogateEvents{
        GroupCreated,
        SlotCreated,
        SourcePropertyInitialized
    }

    override val identity:  CTXIdentity<KSurrogate<T>> = subIdentity(this, receiver)

    val classRecord = TypeData.createByKClass(receiver::class)
    val classInfo = overallInfoFromType<T>(ClassRole.Receiver, classRecord.kType)



    val backingMap: MutableMap<String, SourcePropertyIO<T, Any>> = mutableMapOf()
   // val dataSources: MutableMap<KClass<*>, DataSource<*>> = mutableMapOf()


    override val entries: MutableSet<MutableMap.MutableEntry<String, SourcePropertyIO<T, Any>>>
        get() = backingMap.entries

    val notifier = callbackManager<SurrogateEvents>()
    val groupCreated = CallbackManager.createPayload<SurrogateEvents, PropertyGroup<T, Any>>(notifier, SurrogateEvents.GroupCreated)

    override fun put(key: String, value: SourcePropertyIO<T, Any>): SourcePropertyIO<T, Any>? {
        return backingMap.put(key, value)
    }

    fun <V: Any> setSourceProperty(property: SourcePropertyIO<T, V>){
//        val group = onPropertyProvided?.invoke(property)

    }

    fun <V: Any> registerGroup(group: PropertyGroup<T, V>): PropertyGroup<T, Any> {
        val casted = group.castOrManaged<PropertyGroup<T, Any>>()

        notifier.trigger(SurrogateEvents.GroupCreated, group)

        return casted
    }


//    fun <V: Any> setValue(propertyName: String,  value: V){
//       backingMap[propertyName]?.sourceProperty?.let {
//           it.setValue(value)
//       }?:run {
//           throwManaged("sourceProperty not found for ${propertyName}")
//       }
//    }
//
//    fun <T2: Any> updateData(dataSource: DataSource<T2>,  receiver: T2){
//      val result =  backingMap.values.map {group-> group.getSlot(dataSource.kClass) }
//        result.forEach {slot->
//            val casted = slot.castOrManaged<PropertySlot<T2, Any>>()
//            val value =  casted.property.extractValue(receiver)
//
//        }
//    }

//    fun registerDataSource(dataSource: DataSource<*>){
//        dataSources.put(dataSource.kClass, dataSource)
//    }

    fun propertyInfo(): String {
        val properties = backingMap.values.map { it.toString() }
        return properties.joinToString(separator = SpecialChars.NewLine.toString())
    }

    override fun toString(): String {
        val text = buildString {
            append(Colour.makeOfColour(Colour.YELLOW, "class "))
            append("${classInfo.simpleName} ${Colour.makeOfColour(Colour.YELLOW, "(")} ${SpecialChars.NewLine}")
            append(propertyInfo())
            append("${SpecialChars.NewLine}${Colour.makeOfColour(Colour.YELLOW, ")")}")
        }
        return text
    }



}
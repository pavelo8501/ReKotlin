package po.misc.reflection.properties

import po.misc.data.helpers.textIfNull

class PropertyAccess<T: Any>(
    override val containerName: String,
    val receiver:T
) : PropertyContainer<T> {


    override val hasReceiver: Boolean
        get() = true


    override val receiverSimpleName: String get() = receiver.textIfNull("N/A"){ it::class.simpleName.toString() }
    override val receiverQualifiedName: String get() = receiver.textIfNull("N/A"){ it::class.qualifiedName.toString() }

    val propertyMap = mutableMapOf<String, PropertyInfo<T>>()
    val updatableMap = mutableMapOf<String, PropertyIO<T>>()

    override fun addProperty(propertyInfo: PropertyInfo<T>){

        val propIO = PropertyIO(propertyInfo, propertyInfo.property.get(receiver))
        updatableMap[propIO.propertyInfo.name] = propIO

        propertyMap[propertyInfo.name] = propertyInfo
    }

    override fun updateData(data: T) {
        updatableMap.values.forEach {
            it.setBufferValue(data)
        }
    }

    override fun readData(): T {
        TODO("Not yet implemented")
    }

    fun getPropertiesChanged(): List<PropertyIO<T>>{
       return updatableMap.values.filter { it.isDifferent && !it.isBufferNull }
    }

}
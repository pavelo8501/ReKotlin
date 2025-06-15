package po.misc.reflection.properties

class PropertyIO<T: Any>(
    val propertyInfo: PropertyInfo<T>,
    val currentValue: Any
) {
    var newValueBuffer: Any? = null

    val isDifferent: Boolean  get() = currentValue != newValueBuffer
    val isBufferNull: Boolean get() = newValueBuffer == null


    fun setBufferValue(receiver: T){
        newValueBuffer = propertyInfo.property.get(receiver)
    }

}
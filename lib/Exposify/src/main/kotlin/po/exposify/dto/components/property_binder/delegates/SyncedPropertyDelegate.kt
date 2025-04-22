package po.exposify.dto.components.property_binder.delegates

import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import java.time.LocalDateTime
import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KMutableProperty1




internal inline fun <DTO : ModelDTO, reified V>  DTO.referencing(value: V): ReadWriteProperty<DTO, V> = when(value::class){
    String::class-> DataModelStringDelegate(this)
    Int::class-> DataModelIntDelegate(this)
    Long::class-> DataModelLongDelegate(this)
    Boolean::class -> DataModelBooleanDelegate(this)
    Float::class -> DataModelFloatDelegate(this)
    Double::class -> DataModelDoubleDelegate(this)
    LocalDateTime::class -> DataModelDateTimeDelegate(this)
    else -> throw IllegalArgumentException("Unsupported type")
} as ReadWriteProperty<DTO, V>

internal class DataModelStringDelegate<DTO: ModelDTO>(private val dto: DTO): SimpleDelegate<DTO, String>(dto)
internal class DataModelIntDelegate<DTO: ModelDTO>(private val dto: DTO): SimpleDelegate<DTO, Int>(dto)
internal class DataModelLongDelegate<DTO: ModelDTO>(private val dto: DTO): SimpleDelegate<DTO, Long>(dto)
internal class DataModelDoubleDelegate<DTO: ModelDTO>(private val dto: DTO): SimpleDelegate<DTO, Double>(dto)
internal class DataModelBooleanDelegate<DTO: ModelDTO>(dto: DTO) : SimpleDelegate<DTO, Boolean>(dto)
internal class DataModelFloatDelegate<DTO: ModelDTO>(dto: DTO) : SimpleDelegate<DTO, Float>(dto)
internal class DataModelDateTimeDelegate<DTO: ModelDTO>(private val dto: DTO): SimpleDelegate<DTO, LocalDateTime>(dto)

sealed class SimpleDelegate<DTO: ModelDTO, V>(private val dto: DTO) : ReadWriteProperty<DTO, V> {
    override fun getValue(thisRef: DTO, property: KProperty<*>) =
        dto.dataContainer.getValue<V>(property.name, property)

    override fun setValue(thisRef: DTO, property: KProperty<*>, value: V) =
        dto.dataContainer.setValue(property.name, property, value)
}

package po.exposify.dto.components.bindings.property_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.interfaces.ModelDTO
import po.misc.types.TypeData
import kotlin.reflect.KMutableProperty1

inline fun <DTO, D, E, reified V: Any>  CommonDTO<DTO, D, E>.binding(
    dataProperty:KMutableProperty1<D, V>,
    entityProperty :KMutableProperty1<E, V>
): PropertyDelegate<DTO, D, E, V>
        where  DTO: ModelDTO, D:DataModel, E : LongEntity
{
    val typeData = TypeData.create<V>()
    val propertyDelegate = PropertyDelegate<DTO, D, E, V>(this, dataProperty, entityProperty, typeData)
    return propertyDelegate
}

inline fun <DTO, D, E, reified V: Any>  CommonDTO<DTO, D, E>.serializedBinding(
    dataProperty:KMutableProperty1<D, V>,
    entityProperty:KMutableProperty1<E, V>,
): SerializedDelegate<DTO, D, E, V>
    where DTO: ModelDTO, D: DataModel, E: LongEntity{
    val typeData = TypeData.create<V>()
    return SerializedDelegate(this, dataProperty, entityProperty, typeData)
}

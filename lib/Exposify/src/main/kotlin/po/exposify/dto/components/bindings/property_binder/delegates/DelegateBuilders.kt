package po.exposify.dto.components.bindings.property_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.interfaces.ModelDTO
import po.misc.reflection.properties.models.PropertyUpdate
import kotlin.reflect.KMutableProperty1

fun <DTO, D, E, V: Any>  CommonDTO<DTO, D, E>.binding(
    dataProperty:KMutableProperty1<D, V>,
    entityProperty :KMutableProperty1<E, V>
): PropertyDelegate<DTO, D, E, V>
        where  DTO: ModelDTO, D:DataModel, E : LongEntity
{
    val propertyDelegate = PropertyDelegate<DTO, D, E, V>(this, dataProperty, entityProperty)
    if(tracker.config.observeProperties){
        propertyDelegate.notifier.subscribe<List<PropertyUpdate<V>>>(tracker, ResponsiveDelegate.UpdateType.PropertyUpdated){
            tracker.propertyUpdated(it.getData())
        }
    }
    return propertyDelegate
}


fun <DTO, D, E, V: Any>  CommonDTO<DTO, D, E>.binding(
    dataProperty:KMutableProperty1<D, V>,
): PropertyDelegate<DTO, D, E, V>
        where  DTO: ModelDTO, D:DataModel, E : LongEntity
{
    val propertyDelegate = PropertyDelegate(this, dataProperty, null)

    if(tracker.config.observeProperties){
        propertyDelegate.notifier.subscribe<List<PropertyUpdate<V>>>(tracker, ResponsiveDelegate.UpdateType.PropertyUpdated){
            tracker.propertyUpdated(it.getData())
        }
    }
    return propertyDelegate
}

fun <DTO, D, E, V: Any>  CommonDTO<DTO, D, E>.binding(
): PropertyDelegate<DTO, D, E, V>
        where  DTO: ModelDTO, D:DataModel, E : LongEntity
{
    val propertyDelegate = PropertyDelegate<DTO, D, E, V>(this, null, null)
    if(tracker.config.observeProperties){
        propertyDelegate.notifier.subscribe<List<PropertyUpdate<V>>>(tracker, ResponsiveDelegate.UpdateType.PropertyUpdated){
            tracker.propertyUpdated(it.getData())
        }
    }
    return propertyDelegate
}

fun <DTO, D, E, V: Any>  CommonDTO<DTO, D, E>.serializedBinding(
    dataProperty:KMutableProperty1<D, V>,
    entityProperty:KMutableProperty1<E, V>,
): SerializedDelegate<DTO, D, E, V>
    where DTO: ModelDTO, D: DataModel, E: LongEntity{

    return SerializedDelegate(this, dataProperty, entityProperty)
}

package po.exposify.dto.components.bindings.property_binder

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.components.bindings.property_binder.delegates.ResponsiveDelegate
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.misc.context.CTX
import kotlin.reflect.KMutableProperty1



//
//inline fun <DTO: ModelDTO, D : DataModel, V : Any>  ResponsiveDelegate<DTO, D, *, V>.getDataProperty(
//    callingContext: CTX,
//    block: KMutableProperty1<D, V>.()->V
//): V {
//    val property = dataPropertyContainer.getValue(callingContext)
//    return  block.invoke(property)
//}
//
//inline fun <DTO: ModelDTO, E : LongEntity, V : Any>  ResponsiveDelegate<DTO, *, E, V>.getEntityProperty(
//    callingContext: CTX,
//    block:KMutableProperty1<E, V>.()->V
//): V {
//     val property = entityPropertyContainer.getValue(callingContext)
//     return  block.invoke(property)
//}



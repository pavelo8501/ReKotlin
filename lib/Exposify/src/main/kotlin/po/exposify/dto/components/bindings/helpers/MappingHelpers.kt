package po.exposify.dto.components.bindings.helpers

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.components.bindings.property_binder.delegates.ComplexDelegate
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased
import po.misc.types.TypeRecord
import kotlin.reflect.KProperty


//fun <T: Any>  TypeRecord<T>.toMapperItem(
//    key: ValueBased,
//    metadata: List<ColumnMetadata>?
//): MapperItem<T>{
//    return  MapperItem(key, this, PropertyMap.createPropertyMap(this).propertyMap, metadata)
//}
//
//fun <T: Any>  List<KProperty<*>>.toMapperItem(key: ValueBased, typeRecord: TypeRecord<T>): MapperItem<T>{
//  return  MapperItem(key, typeRecord, PropertyMap.createPropertyMap<T>(this).propertyMap, null)
//}
//
//fun <T: Any>  List<ValidatableBase<T>>.toMapperItems(
//   componentType :  Identifiable
//): Pair<Identifiable, List<MapperItem<T>>> {
//   val result = map {
//       it.mapperItem
//    }
// return  Pair(componentType, result)
//}
//
//@JvmName("toMapperItemsComplexDelegate")
//fun <DTO, D, E, F_DTO, FD, FE> List<ComplexDelegate<DTO, D, E, F_DTO, FD, FE>>.toMapperItems(
//
//): List<MapperItem<FE>> where DTO: ModelDTO, D: DataModel, E: LongEntity, F_DTO: ModelDTO, FD: DataModel, FE: LongEntity{
//
//  val result =  map{delegate->
//      MapperItem.fromPropertyMapperItem(delegate.type)
//    }
//    return result
//}


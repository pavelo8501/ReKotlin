package po.exposify.dto.helpers

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.SourceObject
import po.exposify.exceptions.InitException
import po.exposify.exceptions.OperationsException
import po.exposify.extensions.getOrInitEx
import po.misc.interfaces.ValueBased
import po.misc.reflection.properties.mappers.models.PropertyRecord

import po.misc.types.TypeRecord
import po.misc.types.castOrThrow

fun <DTO : ModelDTO, D : DataModel, E : LongEntity>  CommonDTO<DTO, D, E>.getPropertyRecord(
    key : ValueBased,
    propertyName: String
): PropertyRecord<*>{
  return  dtoClass.config.propertyMap.getPropertyRecord(type, propertyName)
      .getOrInitEx("Property name $propertyName not found in propertyMap")
}

fun <DTO : ModelDTO, D : DataModel, E : LongEntity, T: Any>  DTOBase<DTO, D, E>.getTypeRecord(
    key : ValueBased
): TypeRecord<T>{
    return config.registry.getRecord<T, OperationsException>(key)
}

fun <DTO : ModelDTO, D : DataModel, E : LongEntity>  CommonDTO<DTO, D, E>.toDto():DTO{
  val typeRecord =  dtoClass.getTypeRecord<DTO,D, E, DTO>(SourceObject.DTO)
  return this.castOrThrow<DTO, InitException>(typeRecord.clazz, "Cast to DTO failure")
}
package po.exposify.dto.helpers

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.ComponentType
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.InitException
import po.exposify.exceptions.OperationsException
import po.exposify.extensions.getOrInitEx
import po.misc.reflection.properties.PropertyRecord
import po.misc.registries.type.TypeRecord
import po.misc.types.castOrThrow

fun <DTO : ModelDTO, D : DataModel, E : LongEntity>  CommonDTO<DTO, D, E>.getPropertyRecord(
    componentType : ComponentType,
    propertyName: String
): PropertyRecord<*, Any?>{
  return  dtoClass.config.propertyMap.getPropertyRecord(componentType, propertyName)
      .getOrInitEx("Property name $propertyName not found in propertyMap")
}

fun <DTO : ModelDTO, D : DataModel, E : LongEntity, T: Any>  DTOBase<DTO, D, E>.getTypeRecord(
    component : ComponentType
): TypeRecord<T>{
    return config.registry.getRecord<T, OperationsException>(component)
}

fun <DTO : ModelDTO, D : DataModel, E : LongEntity>  CommonDTO<DTO, D, E>.toDto():DTO{
  val typeRecord =  dtoClass.getTypeRecord<DTO,D, E, DTO>(ComponentType.DTO)
  return this.castOrThrow<DTO, InitException>(typeRecord.clazz, "Cast to DTO failure")
}
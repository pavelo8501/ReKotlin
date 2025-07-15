package po.exposify.dto.helpers

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.castOrOperations
import po.exposify.extensions.getOrInit
import po.misc.interfaces.ValueBased
import po.misc.reflection.mappers.models.PropertyRecord
import po.misc.types.TypeRecord

fun <DTO : ModelDTO, D : DataModel, E : LongEntity>  CommonDTO<DTO, D, E>.getPropertyRecord(
    key : ValueBased,
    propertyName: String
): PropertyRecord<*>{
  return  dtoClass.config.propertyMap.getPropertyRecord(key, propertyName)
      .getOrInit("Property name $propertyName not found in propertyMap")
}

fun <DTO : ModelDTO, D : DataModel, E : LongEntity, T: Any>  DTOBase<DTO, D, E>.getTypeRecord(
    key : ValueBased
): TypeRecord<T>?{
    return config.registry.getRecord<T>(key)
}


fun <DTO, D, E> CommonDTO<DTO, D, E>.asDTO():DTO where DTO : ModelDTO, D : DataModel, E : LongEntity{
    return castOrOperations(typeData.kClass)
}


fun <DTO: ModelDTO, D: DataModel, E: LongEntity> DTO.asCommonDTO(
    dtoClass: DTOBase<DTO, D, E>
): CommonDTO<DTO, D, E> =  castOrOperations<CommonDTO<DTO, D, E>>(dtoClass)
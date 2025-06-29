package po.exposify.dto.helpers

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.SourceObject
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.operationsException
import po.exposify.exceptions.throwOperations
import po.exposify.extensions.getOrInit
import po.exposify.extensions.getOrOperations
import po.misc.interfaces.ValueBased
import po.misc.reflection.mappers.models.PropertyRecord

import po.misc.types.TypeRecord
import po.misc.types.castOrThrow

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

fun <DTO : ModelDTO, D: DataModel, E: LongEntity> CommonDTO<DTO, D, E>.toDto(
    dtoClass: DTOBase<DTO, D, E>,
):DTO {
    val typeRecord = dtoClass.getTypeRecord<DTO, D, E, DTO>(SourceObject.DTO)
        .getOrOperations("Type record not found for key:${SourceObject.DTO}", this)
    val fromClassKey = typeRecord.typeKey
    val commonDtoKey = dtoType.typeKey

    if (fromClassKey != commonDtoKey) {
        val message = "typeKeys do not match. CommonDtoKey = $commonDtoKey. Key obtained from class = $fromClassKey"
        throwOperations(message, ExceptionCode.REFLECTION_ERROR)
    }
    return castOrThrow<DTO, OperationsException>(typeRecord.clazz) {
        operationsException("CommonDTO<DTO, D, E> to DTO cast failure", ExceptionCode.CAST_FAILURE)
    }
}

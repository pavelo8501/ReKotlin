package po.exposify.dto.helpers

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.components.MultipleRepository
import po.exposify.dto.components.SingleRepository
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.ComponentType
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.OperationsException
import po.exposify.extensions.castOrOperationsEx
import po.exposify.extensions.getOrInitEx
import po.misc.reflection.properties.PropertyRecord
import po.misc.registries.type.TypeRecord


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



fun <DTO, DATA, ENTITY, F_DTO, FD, FE> CommonDTO<DTO, DATA, ENTITY>.getMultipleRepository(
    childClass: DTOClass<F_DTO, FD, FE>
):MultipleRepository<DTO, DATA, ENTITY, F_DTO, FD, FE>?
        where DTO: ModelDTO, DATA: DataModel, ENTITY: LongEntity,
              F_DTO:ModelDTO, FD: DataModel, FE : LongEntity{
    val oneToMany =  this.repositories.values.firstOrNull{ it.cardinality == Cardinality.ONE_TO_MANY }
    return oneToMany?.castOrOperationsEx("Unable to cast in multipleRepository helper")
}

fun <DTO, DATA, ENTITY, F_DTO, FD, FE> CommonDTO<DTO, DATA, ENTITY>.getSingleRepository(
    childClass: DTOClass<F_DTO, FD, FE>
): SingleRepository<DTO, DATA, ENTITY, F_DTO, FD, FE>?
        where DTO: ModelDTO, DATA: DataModel, ENTITY: LongEntity,
              F_DTO:ModelDTO, FD: DataModel, FE : LongEntity{
    val oneToOne =  this.repositories.values.firstOrNull{ it.cardinality == Cardinality.ONE_TO_ONE }
    return oneToOne?.castOrOperationsEx("Unable to cast in singleRepository helper")
}


//// Helper for selection
//fun <DTO : ModelDTO, D : DataModel, E : LongEntity> selectEntity(
//    id: Long,
//    entityProvider: (Long) -> E,
//    dtoFactory: DtoFactory<DTO, D, E>
//): DTO {
//    val entity = entityProvider(id)
//    return dtoFactory.fromEntity(entity)
//}
//
//fun <DTO : ModelDTO, D : DataModel, E : LongEntity> insertFromData(
//    data: D,
//    dtoFactory: DtoFactory<DTO, D, E>,
//    updater: DtoUpdater<DTO, D, E>
//): E {
//    val dto = dtoFactory.fromData(data)
//    updater.syncAll(dto)
//    return dtoFactory.createEntity(dto)
//}

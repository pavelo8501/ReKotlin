package po.exposify.dto.helpers

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dao.classes.ExposifyEntityClass
import po.exposify.dao.helpers.getExposifyEntityCompanion
import po.exposify.dto.DTOBase
import po.exposify.dto.components.DTOConfig
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.SourceObject
import po.exposify.exceptions.InitException
import po.misc.exceptions.ManagedException
import po.misc.exceptions.SelfThrownException
import po.misc.reflection.properties.PropertyMapper
import po.misc.reflection.properties.mappers.helpers.createPropertyMap
import po.misc.reflection.properties.toPropertyMap
import po.misc.registries.type.TypeRegistry
import po.misc.types.TypeRecord
import po.misc.types.castBaseOrThrow
import po.misc.types.castOrThrow
import kotlin.reflect.full.companionObjectInstance




inline fun <reified DTO,  reified D, reified E> DTOBase<DTO, D, E>.configuration(
    noinline block:  DTOConfig<DTO, D, E>.() -> Unit
): Unit where DTO: ModelDTO, D: DataModel, E: LongEntity {

    val registry = TypeRegistry()

    val dtoType = registry.addRecord<DTO>(SourceObject.DTO.provideType(TypeRecord.createRecord(SourceObject.DTO)))
    val dataType = registry.addRecord<D>( SourceObject.Data.provideType(TypeRecord.createRecord(SourceObject.Data)))
    val entityType = registry.addRecord<E>(SourceObject.Entity.provideType(TypeRecord.createRecord(SourceObject.Entity)))

    val entityModel =  getExposifyEntityCompanion<E, InitException>()
    val entityMetadata =  entityModel.analyzeExposedTableMetadata<E>()

    val propertyMapper = PropertyMapper()
    propertyMapper.addMapperRecord(SourceObject.Entity,  createPropertyMap(entityType, entityMetadata))
    propertyMapper.addMapperRecord(SourceObject.Data,  createPropertyMap(dataType))
    propertyMapper.addMapperRecord(SourceObject.DTO,  createPropertyMap(dtoType))

    val newConfiguration = DTOConfig(registry, propertyMapper, entityModel, this)
    configParameter = newConfiguration
    block.invoke(config)
    initializationComplete()
    setupValidation(propertyMapper, registry)
}
package po.exposify.dto.configuration

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.DatabaseManager
import po.exposify.dao.helpers.getExposifyEntityCompanion
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.RootDTO
import po.exposify.dto.components.DTOConfig
import po.exposify.dto.components.bindings.helpers.shallowDTO
import po.exposify.dto.enums.DTOClassStatus
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.SourceObject
import po.exposify.exceptions.InitException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.throwInit
import po.misc.reflection.mappers.PropertyMapper
import po.misc.reflection.mappers.helpers.createPropertyMap
import po.misc.registries.type.TypeRegistry
import po.misc.types.TypeRecord

inline fun <reified DTO,  reified D, reified E> DTOBase<DTO, D, E>.configuration(
    noinline block:  DTOConfig<DTO, D, E>.() -> Unit
) where DTO: ModelDTO, D: DataModel, E: LongEntity {

    if(status == DTOClassStatus.Uninitialized){
        val registry = TypeRegistry()

        registry.addRecord<DTO>(SourceObject.DTO.provideType(dtoType))
        val dataType = registry.addRecord<D>( SourceObject.Data.provideType(TypeRecord.createRecord(SourceObject.Data)))
        val entityType = registry.addRecord<E>(SourceObject.Entity.provideType(TypeRecord.createRecord(SourceObject.Entity)))
        registry.addRecord<CommonDTO<DTO, D, E>>(
            SourceObject.CommonDTOType.provideType<DTO, D, E>(
                TypeRecord.createRecord(SourceObject.CommonDTOType)
            )
        )
        val entityModel =  getExposifyEntityCompanion<E, InitException>()
        val entityMetadata =  entityModel.analyzeExposedTableMetadata<E>()
        val propertyMapper = PropertyMapper()
        propertyMapper.addMapperRecord(SourceObject.Entity,  createPropertyMap(entityType, entityMetadata))
        propertyMapper.addMapperRecord(SourceObject.Data,  createPropertyMap(dataType))
        propertyMapper.addMapperRecord(SourceObject.DTO,  createPropertyMap(dtoType))
        configParameter = DTOConfig(registry, propertyMapper, entityModel, this)
        block.invoke(config)

        val shallowDTO = shallowDTO()
        val relationDelegates = shallowDTO.bindingHub.getRelationDelegates()
        relationDelegates.forEach {relationDelegate->
            val foreignClass = relationDelegate.foreignClass
            foreignClass.initialization()
            config.addHierarchMember(foreignClass)
        }
        initializationComplete(shallowDTO)
    }
}
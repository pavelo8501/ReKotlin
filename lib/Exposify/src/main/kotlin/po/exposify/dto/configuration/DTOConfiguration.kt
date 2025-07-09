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

        val dataType =  config.registry.addRecord<D>( SourceObject.Data.provideType(TypeRecord.createRecord(SourceObject.Data)))
        val entityType = config.registry.addRecord<E>(SourceObject.Entity.provideType(TypeRecord.createRecord(SourceObject.Entity)))

        val common = config.registry.addRecord<CommonDTO<DTO, D, E>>(SourceObject.CommonDTOType.provideType(TypeRecord.createRecord(SourceObject.CommonDTOType)))
        commonTypeParameter = common

        config.entityModelBacking =  getExposifyEntityCompanion<E, InitException>()
        val entityMetadata =  config.entityModel.analyzeExposedTableMetadata<E>()
        config.propertyMap.addMapperRecord(SourceObject.Entity,  createPropertyMap(entityType, entityMetadata))
        config.propertyMap.addMapperRecord(SourceObject.Data,  createPropertyMap(dataType))
        block.invoke(config)

        val shallowDTO = shallowDTO()
        val relationDelegates = shallowDTO.hub.relationDelegates
        relationDelegates.forEach {relationDelegate->
            val foreignClass = relationDelegate.foreignClass
            foreignClass.initialization()
            config.addHierarchMember(foreignClass)
        }
        initializationComplete(shallowDTO)
    }
}
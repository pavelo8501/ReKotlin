package po.exposify.dto.configuration

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dao.helpers.getExposifyEntityCompanion
import po.exposify.dto.DTOBase
import po.exposify.dto.components.DTOConfig
import po.exposify.dto.components.bindings.helpers.shallowDTO
import po.exposify.dto.enums.DTOClassStatus
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.CommonDTOType
import po.exposify.dto.models.SourceObject
import po.exposify.exceptions.InitException
import po.misc.reflection.mappers.helpers.createPropertyMap
import po.misc.types.TypeData

inline fun <reified DTO,  reified D, reified E> DTOBase<DTO, D, E>.configuration(
    noinline block:  DTOConfig<DTO, D, E>.() -> Unit
) where DTO: ModelDTO, D: DataModel, E: LongEntity {

    if(status == DTOClassStatus.Uninitialized){

        provideDataType(TypeData.create<D>())
        provideEntityType(TypeData.create<E>())
        val commonDTOType: CommonDTOType<DTO, D, E> = CommonDTOType.create()
        provideCommonDTOType(commonDTOType)

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
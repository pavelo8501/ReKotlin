package po.exposify.dto.helpers

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.SourceObject
import po.exposify.exceptions.OperationsException
import po.misc.reflection.properties.PropertyMapper
import po.misc.reflection.properties.mappers.helpers.mapperCheck
import po.misc.registries.type.TypeRegistry
import po.misc.validators.MappingValidator
import po.misc.validators.helpers.conditionTrue
import po.misc.validators.helpers.containsSame
import po.misc.validators.helpers.sequentialByInstance
import po.misc.validators.helpers.sequentialBySource

inline fun <reified DTO,  reified D, reified E> DTOBase<DTO, D, E>.setupValidation(
    propertyMapper : PropertyMapper,
    typeRegistry: TypeRegistry
): Boolean  where DTO: ModelDTO, D: DataModel, E: LongEntity {

    val shallowDto = config.dtoFactory.createDto()
    val entityRecord = propertyMapper.getMapperRecord<E, OperationsException>(SourceObject.Entity)
    val typeRecord = propertyMapper.getMapperRecord<DTO, OperationsException>(SourceObject.DTO)
    propertyMapper.propertyValidator.ignoreProperty {
        it.propertyName == "id"
    }

    val responsiveValidation = shallowDto.bindingHub.createValidation()
    val nonNullProperties =  propertyMapper.mapperCheck(
        "Property bindings",
        entityRecord,
        responsiveValidation).sequentialBySource{ sourceRecord, validatable ->
        containsSame(sourceRecord, validatable)
    }
    propertyMapper.executeCheck(nonNullProperties, MappingValidator.MappedPropertyValidator.NON_NULLABLE)

    val parentValidation =  parentValidation<DTO,D,E, ModelDTO, DataModel, LongEntity>(shallowDto)
    val parenBindingsSet = propertyMapper.mapperCheck(
        "Parent bindings",
        entityRecord,
        parentValidation).sequentialBySource{ sourceRecord, validatable ->
        containsSame(sourceRecord, validatable)
    }
    propertyMapper.executeCheck(parenBindingsSet, MappingValidator.MappedPropertyValidator.PARENT_SET)

    shallowDto.bindingHub.getAttachedForeignDelegates().forEach {
        val attachedValidation = createValidation(it)
        val attachedBindingsSet = propertyMapper.mapperCheck(
            "Attached parent configured",
            entityRecord,
            attachedValidation).sequentialByInstance { validatable, sourceRecord ->
            conditionTrue(validatable,"${validatable.instance.componentName} not initialized. Instantiate service before this call"){
                initialized == true
            }
        }
        propertyMapper.executeCheck(attachedBindingsSet, MappingValidator.MappedPropertyValidator.FOREIGN_SET)
    }
    return true
}


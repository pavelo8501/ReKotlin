package po.exposify.dto.helpers

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.components.bindings.BindingHub
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.CheckedProperty
import po.exposify.dto.models.SourceObject
import po.exposify.exceptions.InitException
import po.exposify.exceptions.OperationsException
import po.lognotify.classes.task.models.TaskConfig
import po.lognotify.extensions.subTask
import po.misc.interfaces.Identifiable
import po.misc.interfaces.asIdentifiable
import po.misc.reflection.mappers.PropertyMapper
import po.misc.reflection.mappers.helpers.mapperCheck
import po.misc.validators.general.ValidatableContainer
import po.misc.validators.general.Validator
import po.misc.validators.mapping.MappingValidator
import po.misc.validators.mapping.helpers.bulkValidator
import po.misc.validators.mapping.helpers.conditionTrue
import po.misc.validators.mapping.helpers.containsSame
import po.misc.validators.mapping.helpers.finalCheckStatus
import po.misc.validators.mapping.helpers.sequentialByInstance
import po.misc.validators.mapping.helpers.sequentialBySource
import po.misc.validators.mapping.models.CheckStatus
import po.misc.validators.mapping.models.ValidationClass

inline fun <reified DTO,  reified D, reified E> DTOBase<DTO, D, E>.setupValidation(
    propertyMapper : PropertyMapper
): Boolean  where DTO: ModelDTO, D: DataModel, E: LongEntity {

    subTask("Setup validation", TaskConfig(actor = component.completeName)) {handler->

        val validator = Validator()
        val entityContainer = ValidatableContainer<CommonDTO<DTO, D, E>>(component , {config.dtoFactory.createDto()})
        val identifyed: Identifiable = asIdentifiable(component.sourceName, "Validator")
        validator.executeCheck(entityContainer){
            try {
                val dto = it()
                dto.bindingHub.subscribe(identifyed, BindingHub.Event.DelegateInitialized){
                    val notification = it
                }
            }catch (ex: InitException){
             val a = ex
            }
        }


        val shallowDto = config.dtoFactory.createDto()
        val entityRecord = propertyMapper.getMapperRecord<E, OperationsException>(SourceObject.Entity)
        val typeRecord = propertyMapper.getMapperRecord<DTO, OperationsException>(SourceObject.DTO)
        propertyMapper.propertyValidator.ignoreProperty {
            it.propertyName == "id"
        }
        val responsiveValidation = shallowDto.bindingHub.createValidation()
        val nonNullProperties = propertyMapper.mapperCheck(
            "Property bindings",
            entityRecord,
            responsiveValidation
        ).sequentialBySource { sourceRecord, validatable ->
            containsSame(sourceRecord, validatable)
        }
        propertyMapper.executeCheck(nonNullProperties, MappingValidator.MappedPropertyValidator.NON_NULLABLE)

        val parentValidation = parentValidation<DTO, D, E, ModelDTO, DataModel, LongEntity>(shallowDto)
        val parenBindingsSet = propertyMapper.mapperCheck(
            "Parent bindings",
            entityRecord,
            parentValidation
        ).sequentialBySource { sourceRecord, validatable ->
            containsSame(sourceRecord, validatable)
        }
        propertyMapper.executeCheck(parenBindingsSet, MappingValidator.MappedPropertyValidator.PARENT_SET)

//        shallowDto.bindingHub.getAttachedForeignDelegates().forEach {
//            val attachedValidation = createValidation(it)
//            val attachedBindingsSet = propertyMapper.mapperCheck(
//                "Attached parent configured",
//                entityRecord,
//                attachedValidation
//            ).sequentialByInstance { validatable, sourceRecord ->
//                conditionTrue(
//                    validatable,
//                    "${validatable.instance.component} not initialized. Instantiate service before this call"
//                ) {
//                    initialized
//                }
//            }
//            propertyMapper.executeCheck(attachedBindingsSet, MappingValidator.MappedPropertyValidator.FOREIGN_SET)
//        }
        val finalStatus = propertyMapper.propertyValidator.reportList.finalCheckStatus()
        return finalStatus != CheckStatus.FAILED
    }.onFail {

    }.resultOrException()
}


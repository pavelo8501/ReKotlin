package po.exposify.dto.configuration

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dao.models.ColumnPropertyData
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.components.bindings.DelegateStatus
import po.exposify.dto.components.bindings.interfaces.DelegateInterface
import po.exposify.dto.components.bindings.property_binder.delegates.ResponsiveDelegate
import po.exposify.dto.components.bindings.relation_binder.delegates.ParentDelegate
import po.exposify.dto.components.bindings.relation_binder.delegates.RelationDelegate
import po.exposify.dto.enums.DTOClassStatus
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.InitException
import po.exposify.exceptions.enums.ExceptionCode
import po.misc.validators.Validator
import po.misc.validators.components.validatorHooks
import po.misc.validators.models.CheckStatus
import po.misc.validators.reports.finalCheckStatus
import po.misc.validators.sequentialValidation
import po.misc.validators.validators.conditionNotNull
import po.misc.validators.validators.conditionTrue

fun <DTO, D, E> DTOBase<DTO, D, E>.setupValidation(
    validatableDTO: CommonDTO<DTO, D, E>,
): CheckStatus where DTO : ModelDTO, D : DataModel, E : LongEntity {
    val dtoClass = this
    val bindingHub = validatableDTO.bindingHub
    val validator = Validator(validatableDTO.dtoClass)

    val reports = validator.build {
            val foreignKeys = dtoClass.commonDTOType.entityType.tableColumnMap
                .getData()
                .filter { it.columnData.isForeignKey }

            val mandatoryFields: List<ColumnPropertyData> =
                dtoClass.commonDTOType.entityType.tableColumnMap.getData().filter {
                    it.columnData.isMandatory
                }

            val relationDelegates  = validatableDTO.bindingHub.relationDelegateMap.values
            val attachedForeign = validatableDTO.bindingHub.attachedForeignMap.values.toList()
            val parentDelegates = validatableDTO.bindingHub.parentDelegateMap.values
            val responsive: List<ResponsiveDelegate<DTO, D, E, *>> = bindingHub.responsiveDelegateMap.values.toList()

            val attachedNames = attachedForeign.map { it.attachedName }.toSet()
            val filteredMandatory =
                mandatoryFields
                    .filter { field -> field.property.name !in attachedNames }
                    .distinctBy { it.property.name }

            sequentialValidation(ValidationCheck.MandatoryProperties.value, filteredMandatory) { field ->
                validatorHooks {
                    onConditionSuccess { delegate ->
                        (delegate as DelegateInterface<DTO, D, E>).updateStatus(DelegateStatus.Initialized)
                    }
                }
                conditionNotNull(field.property.name, "Entity required, but missing") {
                    responsive.firstOrNull { field.compareAndGet(it.property.name) != null }
                }
            }

            sequentialValidation(ValidationCheck.ForeignKeys.value, foreignKeys) { foreignKey ->
                conditionNotNull(foreignKey.columnData.columnName, "Foreign key not configured") {
                    parentDelegates.firstOrNull { it.name == foreignKey.columnData.columnName }
                }
//                parentDelegates.forEach { parentDelegate ->
//                    conditionNotNull("Parent DTO resolved", "Parent DTO not defined") {
//                        parentDelegate.commonDTO
//                    }
//                }
            }

            sequentialValidation(ValidationCheck.AttachedForeign.value, attachedForeign) { foreign ->
                conditionTrue(foreign.contextName, "Attached foreign not configured") {
                    foreign.dtoBase.status == DTOClassStatus.Initialized
                }
            }

            reports.forEach {
                log(it)
            }

            if (overallResult == CheckStatus.PASSED) {

                relationDelegates.forEach { relation ->
                    val aa = relation.dtoClass.initialization()

                    val newMember = dtoConfiguration.addHierarchMember(dtoClass, relation.dtoClass)
                    newMember.runValidation(validatableDTO)
                }
            }
        }
    return reports.finalCheckStatus()
}

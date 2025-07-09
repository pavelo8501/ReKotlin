package po.exposify.dto.configuration

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.withHook
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.components.bindings.DelegateStatus
import po.exposify.dto.enums.DTOClassStatus
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.SourceObject
import po.exposify.exceptions.InitException
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.initAbnormal
import po.exposify.exceptions.operationsException
import po.lognotify.classes.action.runInlineAction
import po.misc.validators.general.Validator
import po.misc.validators.general.models.CheckStatus
import po.misc.validators.general.reports.ReportRecord
import po.misc.validators.general.reports.ValidationReport
import po.misc.validators.general.reports.finalCheckStatus
import po.misc.validators.general.sequentialValidation
import po.misc.validators.general.validation
import po.misc.validators.general.validators.conditionTrue
import po.misc.validators.general.validators.validatorHooks

fun <DTO, D, E> DTOBase<DTO, D, E>.setupValidation(
    validatableDTO : CommonDTO<DTO, D, E>
): CheckStatus  where DTO: ModelDTO, D: DataModel, E: LongEntity
        = runInlineAction("setupValidation") { handler ->
    val bindingHub = validatableDTO.hub
    val validator = Validator()
    val entityRecord =  config.propertyMap.getMapperRecord<E, InitException>(SourceObject.Entity){ initAbnormal(it, this) }
    val responsiveDelegates = bindingHub.responsiveDelegates
    val relationsDelegates  = bindingHub.relationDelegates
    val parentDelegates = bindingHub.parentDelegates
    val attachedForeignDelegates = bindingHub.attachedForeignDelegates
    val validationSequence = sequenceOf(
        ValidationCheck.AttachedForeign,
        ValidationCheck.MandatoryProperties,
        ValidationCheck.ForeignKeys,
    )

    val reports = validator.validate(completeName, this) {
        validationSequence.forEach {validation->
            when (validation) {
                ValidationCheck.AttachedForeign->{
                    sequentialValidation(validation.value, attachedForeignDelegates){foreign->
                        validatorHooks {
                            onSuccess {
                                it.updateStatus(DelegateStatus.Initialized)
                            }
                        }
                        conditionTrue(foreign.foreignClass.identity.sourceName, "Attached foreign not configured"){
                            foreign.foreignClass.status == DTOClassStatus.Initialized
                        }
                    }
                }
                ValidationCheck.MandatoryProperties->{
                    val mandatoryFields = entityRecord.columnMetadata.filter { !it.isNullable && !it.hasDefault && !it.isPrimaryKey && !it.isForeignKey }
                    sequentialValidation(validation.value, mandatoryFields){ field->
                        validatorHooks {
                            onResult {
                                if(it == CheckStatus.PASSED){
                                    responsiveDelegates.forEach {delegate->
                                        delegate.updateStatus(DelegateStatus.Initialized)
                                    }
                                }
                            }
                        }
                        conditionTrue(field.columnName, "Entity non nullable, no defaults. But missing") {
                            mandatoryFieldsSetup(field, responsiveDelegates, attachedForeignDelegates) != null
                        }
                    }
                }
                ValidationCheck.ForeignKeys->{
                    val foreignKeys = entityRecord.columnMetadata.filter { it.isForeignKey }
                    sequentialValidation(validation.value, foreignKeys){foreignKey->
                        validatorHooks {
                            onResult {
                                if(it == CheckStatus.PASSED){
                                    parentDelegates.forEach {delegate->
                                        delegate.updateStatus(DelegateStatus.Initialized)
                                    }
                                }
                            }
                        }
                        conditionTrue(foreignKey.columnName, "Foreign key not configured"){
                            parentInitialized(foreignKey, parentDelegates)
                        }
                    }
                }
            }
        }
    }
    reports.forEach { report ->
        handler.logFormatted(report) {
            echo(ValidationReport.Header)
            getRecords().forEach { record -> record.echo(ReportRecord.GeneralTemplate) }
            echo(ValidationReport.Footer)
        }
    }
    reports.finalCheckStatus()
}

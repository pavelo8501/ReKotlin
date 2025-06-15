package po.exposify.dto.helpers


import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOBase
import po.exposify.dto.components.bindings.BindingHub
import po.exposify.dto.components.bindings.DelegateStatus
import po.exposify.dto.components.bindings.relation_binder.delegates.RelationDelegate
import po.exposify.dto.enums.Delegates
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.SourceObject
import po.exposify.exceptions.OperationsException
import po.lognotify.classes.action.runInlineAction
import po.lognotify.classes.notification.NotifierHub
import po.misc.data.PrintableBase
import po.misc.interfaces.Identifiable
import po.misc.interfaces.asIdentifiable
import po.misc.interfaces.asIdentifiableClass
import po.misc.reflection.mappers.PropertyMapper
import po.misc.validators.general.Validator
import po.misc.validators.general.models.CheckStatus
import po.misc.validators.general.reports.ReportRecord
import po.misc.validators.general.reports.ValidationReport
import po.misc.validators.general.reports.ValidationReport.Companion.Footer
import po.misc.validators.general.reports.ValidationReport.Companion.Header
import po.misc.validators.general.reports.finalCheckStatus
import po.misc.validators.general.validation
import po.misc.validators.general.validators.conditionTrue


fun <DTO, D, E> DTOBase<DTO, D, E>.setupValidation(
    propertyMapper : PropertyMapper
): Boolean  where DTO: ModelDTO, D: DataModel, E: LongEntity
        = runInlineAction(this, "setupValidation") {handler->

    var result: Boolean = false
    var relationDelegates: List<RelationDelegate<DTO, D, E, *, *, *, *>> = listOf()

    fun delegateValidation(container: BindingHub.ListData<DTO, D, E>): Boolean {
        val bindingHub = container.hostingDTO.bindingHub
        val validator = Validator()
        val entityRecord = propertyMapper.getMapperRecord<E, OperationsException>(SourceObject.Entity)
        val reports = validator.validate(this.completeName, this) {
            val categorize = container.delegates.groupBy { it.module.moduleName }
            categorize.forEach { (key, value) ->
                val enum = key as Delegates
                when (enum) {
                    Delegates.AttachedForeignDelegate -> {

                        validation("AttachedForeign initialized", bindingHub.getAttachedForeignDelegates() ) {delegates->
                            delegates.forEach { attachedDelegate ->
                                conditionTrue("Service initialized") {
                                    attachedDelegate.foreignClass.initialized
                                }
                            }
                            if(overallStatus == CheckStatus.PASSED){
                                delegates.forEach { it.updateStatus(DelegateStatus.Initialized) }
                            }
                        }
                    }

                    Delegates.RelationDelegate -> {
                        relationDelegates = bindingHub.getRelationDelegates()
                    }

                    Delegates.ResponsiveDelegate -> {

                       val mandatory =  entityRecord.columnMetadata.filter { !it.isNullable && !it.hasDefault && !it.isPrimaryKey && !it.isForeignKey }
                       validation("Mandatory properties set", bindingHub.getResponsiveDelegates()){delegates->
                           mandatory.forEach { mandatoryProperty->
                               conditionTrue(mandatoryProperty.columnName, "Entity non nullable, no defaults. But missing"){
                                   responsiveInitialized(mandatoryProperty, delegates)
                               }
                           }
                           if(overallStatus == CheckStatus.PASSED){
                               delegates.forEach { it.updateStatus(DelegateStatus.Initialized) }
                           }
                        }
                    }

                    Delegates.ParentDelegate -> {
                        val foreignKeys =  entityRecord.columnMetadata.filter { it.isForeignKey }
                        validation("Parent(Foreign properties assigned)", bindingHub.getParentDelegates()){delegates->
                            foreignKeys.forEach { foreignKey ->
                                parentInitialized(foreignKey, delegates)
                            }
                            if(overallStatus == CheckStatus.PASSED){
                                delegates.forEach { it.updateStatus(DelegateStatus.Initialized) }
                            }
                        }
                    }
                }
            }
        }

        reports.forEach {report->
            handler.log<ValidationReport>(report) {
                printTemplate(Header)
                getRecords().forEach {record-> record.printTemplate(ReportRecord.GeneralTemplate)}
                printTemplate(Footer)
            }
        }
        return reports.finalCheckStatus() != CheckStatus.FAILED
    }

    notifier.subscribe<BindingHub.ListData<DTO, D, E>>(this, DTOBase.Events.DelegateRegistrationComplete){
        val data = it.getData()
        result = delegateValidation(data)
    }

    //Triggers configuration block and validation
    val shallowDTO = config.dtoFactory.createDto()

    if(result){
        relationDelegates.forEach {
            it.resolveForeign()
        }
    }

    return result
}


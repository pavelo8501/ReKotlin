package po.exposify.dto.helpers


import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOBase
import po.exposify.dto.components.bindings.BindingHub
import po.exposify.dto.components.bindings.property_binder.delegates.AttachedForeignDelegate
import po.exposify.dto.components.bindings.property_binder.delegates.ParentDelegate
import po.exposify.dto.components.bindings.property_binder.delegates.ResponsiveDelegate
import po.exposify.dto.components.bindings.relation_binder.delegates.RelationDelegate
import po.exposify.dto.enums.Delegates
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.SourceObject
import po.exposify.exceptions.OperationsException
import po.misc.interfaces.Identifiable
import po.misc.interfaces.asIdentifiable
import po.misc.reflection.mappers.PropertyMapper
import po.misc.validators.general.Validator
import po.misc.validators.general.models.CheckStatus
import po.misc.validators.general.reports.ValidationReport
import po.misc.validators.general.reports.finalCheckStatus
import po.misc.validators.general.validation
import po.misc.validators.general.validators.conditionTrue


fun <DTO, D, E> DTOBase<DTO, D, E>.setupValidation(
    propertyMapper : PropertyMapper
): Boolean  where DTO: ModelDTO, D: DataModel, E: LongEntity {

    var result: Boolean = false

    fun onDelegatesRegistered(container: BindingHub.ListData<DTO, D, E>): Boolean {

        var relationDelegates: List<RelationDelegate<DTO, D, E, *, *, *, *>>
        val validator = Validator()
        val entityRecord = propertyMapper.getMapperRecord<E, OperationsException>(SourceObject.Entity)

        val reports = validator.validate(this.component.completeName, component) {
            val categorize = container.delegates.groupBy { it.module.moduleName }
            categorize.forEach { (key, value) ->
                val enum = key as Delegates
                when (enum) {
                    Delegates.AttachedForeignDelegate -> {
                        val attachedForeignDelegates = value.filterIsInstance<AttachedForeignDelegate<DTO, D, E, *, *, *, >>()

                        validation("AttachedForeign initialized", attachedForeignDelegates) {
                            attachedForeignDelegates.forEach { attachedDelegate ->
                                conditionTrue("Service initialized") {
                                    attachedDelegate.foreignClass.initialized
                                }
                            }
                        }
                    }

                    Delegates.RelationDelegate -> {
                        relationDelegates = value.filterIsInstance<RelationDelegate<DTO, D, E, *, *, *, *>>()
                    }

                    Delegates.ResponsiveDelegate -> {
                        val responsiveDelegates = value.filterIsInstance<ResponsiveDelegate<DTO, D, E, *, >>()
                        val mandatory =  entityRecord.columnMetadata.filter { !it.isNullable && !it.hasDefault && !it.isPrimaryKey }
                       validation("Mandatory properties set", mandatory){
                           mandatory.forEach { mandatoryProperty->
                               conditionTrue(mandatoryProperty.columnName){
                                   responsiveInitialized(mandatoryProperty, responsiveDelegates)
                               }
                           }
                        }
                    }

                    Delegates.ParentDelegate -> {
                        val parentDelegates = value.filterIsInstance<ParentDelegate<DTO, D, E, *, *, *>>()
                        val foreignKeys =  entityRecord.columnMetadata.filter { it.isForeignKey }
                        validation("Parent(Foreign properties assigned)", parentDelegates){
                            foreignKeys.forEach { foreignKey ->
                                parentInitialized(foreignKey, parentDelegates)
                            }
                        }
                    }
                }
            }
        }
        reports.forEach {
            it.printReport()
        }
        return reports.finalCheckStatus() != CheckStatus.FAILED
    }

    val identifiable: Identifiable = asIdentifiable("setupValidation", component.completeName)
    callbackForwarder.subscribe(identifiable, DTOBase.Events.DelegateRegistrationComplete) {
        result = onDelegatesRegistered(it)
    }
    val shallowDTO = config.dtoFactory.createDto()

    return result
}


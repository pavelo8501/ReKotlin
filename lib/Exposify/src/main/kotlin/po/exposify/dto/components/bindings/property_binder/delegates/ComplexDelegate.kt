package po.exposify.dto.components.bindings.property_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOClass
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.components.DTOFactory
import po.exposify.dto.components.proFErty_binder.EntityUpdateContainer
import po.exposify.dto.components.bindings.property_binder.enums.UpdateMode
import po.exposify.dto.components.bindings.relation_binder.delegates.RelationDelegate
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.helpers.toDto
import po.exposify.dto.interfaces.ComponentType
import po.exposify.dto.interfaces.IdentifiableComponent
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.castOrInitEx
import po.exposify.extensions.castOrOperationsEx
import po.exposify.extensions.getOrOperationsEx
import po.exposify.extensions.withTransactionIfNone
import po.lognotify.TasksManaged
import po.lognotify.extensions.subTask
import po.misc.types.castOrThrow
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty


sealed class ComplexDelegate<DTO, D, E, F_DTO, FD,  FE>(
    protected val hostingDTO : CommonDTO<DTO, D, E>,
    protected val foreignClass: DTOBase<F_DTO, FD, FE>,
):TasksManaged, IdentifiableComponent
        where D: DataModel, E: LongEntity, DTO : ModelDTO,
              F_DTO: ModelDTO, FD : DataModel,  FE: LongEntity
{

    override val type: ComponentType = ComponentType.ComplexDelegate
    abstract override val qualifiedName : String

    protected var onPropertyInitialized: ((KProperty<*>)-> Unit)? = null
    private var propertyParameter : KProperty<*>? = null
        set(value) {
            field = value
            onPropertyInitialized?.invoke(property)
        }
    val property: KProperty<Any?> get() = propertyParameter.getOrOperationsEx("Property not yet initialized")
    val propertyName : String get() = propertyParameter?.name?:""

    private var foreignDTOParameter: CommonDTO<F_DTO, FD, FE>? = null
    protected val foreignDTO: CommonDTO<F_DTO, FD, FE>
        get() = foreignDTOParameter.getOrOperationsEx("Foreign dto is not yet assigned")

    init {
        foreignClass.onInitComplete = {
            foreignClass.config.dtoFactory.notificator.subscribe(this, DTOFactory.FactoryEvents.ON_INITIALIZED){parentDto->
                foreignDTOParameter = parentDto
                sourceProvided(parentDto.toDto())
            }
        }
    }

    protected abstract fun sourceProvided(dto: F_DTO)

    operator fun getValue(thisRef: DTO, property: KProperty<*>): F_DTO{
        propertyProvided(property)
        return  foreignDTO.toDto()
    }
    operator fun provideDelegate(thisRef: DTO, property: KProperty<*>): ComplexDelegate<DTO, D, E, F_DTO, FD, FE> {
        propertyProvided(property)
        return this
    }
    private fun propertyProvided(property: KProperty<Any?>){
        if(propertyParameter == null){
            propertyParameter = property
            hostingDTO.bindingHub.setComplexBinding(this.castOrInitEx())
        }
    }
}


class ForeignIDDelegate<DTO, D, E, F_DTO, FD, FE>(
    hostingDTO: CommonDTO<DTO, D, E>,
    foreignClass: DTOBase<F_DTO, FD, FE>,
    val dataProperty : KMutableProperty1<D, Long>,
): ComplexDelegate<DTO, D, E, F_DTO, FD, FE>(hostingDTO, foreignClass)
    where D: DataModel, E: LongEntity, DTO: ModelDTO,
          F_DTO: ModelDTO, FD: DataModel, FE: LongEntity
{
    override val qualifiedName: String get() = "ForeignIDDelegate[${hostingDTO.dtoName}]"
    override fun sourceProvided(dto:F_DTO) {
        hostingDTO.logger.info("Received update from ${foreignClass.qualifiedName}")
        println(dto)
    }

}


class ParentDelegate<DTO, D, ENTITY, F_DTO, FD, FE>(
    dto: CommonDTO<DTO, D, ENTITY>,
    foreignClass: DTOBase<F_DTO, FD, FE>,
    val foreignDTOProvider: ((F_DTO)-> Unit)?
): ComplexDelegate<DTO, D, ENTITY, F_DTO, FD, FE>(dto, foreignClass)
        where D: DataModel, ENTITY: LongEntity, DTO : ModelDTO, F_DTO: ModelDTO, FD : DataModel, FE: LongEntity
{

    override val qualifiedName: String get() = "ParentDelegate[${hostingDTO.dtoName}]"
    override fun sourceProvided(dto: F_DTO) {
        hostingDTO.logger.info("Received update from ${foreignClass.qualifiedName}")
        println(dto)
        foreignDTOProvider?.invoke(dto)

    }

}



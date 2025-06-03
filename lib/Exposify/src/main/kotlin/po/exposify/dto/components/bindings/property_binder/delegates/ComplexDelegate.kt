package po.exposify.dto.components.bindings.property_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.components.DTOFactory
import po.exposify.dto.helpers.toDto
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.Component
import po.exposify.dto.models.ComponentType
import po.exposify.dto.models.componentInstance
import po.exposify.dto.models.provideType
import po.exposify.extensions.castOrInitEx
import po.exposify.extensions.getOrInitEx
import po.exposify.extensions.getOrOperationsEx
import po.lognotify.TasksManaged
import po.misc.interfaces.Identifiable
import po.misc.reflection.properties.mappers.models.PropertyMapperRecord
import po.misc.reflection.properties.mappers.models.PropertyRecord
import po.misc.types.TypeRecord
import kotlin.reflect.KProperty


sealed class ComplexDelegate<DTO, D, E, F_DTO, FD,  FE>(
    internal val hostingDTO : CommonDTO<DTO, D, E>,
    protected val foreignClass: DTOBase<F_DTO, FD, FE>,
    internal val component: Component
): TasksManaged, Identifiable
        where D: DataModel, E: LongEntity, DTO : ModelDTO,
              F_DTO: ModelDTO, FD : DataModel,  FE: LongEntity
{

    override val componentName: String get()= component.componentName
    override val completeName: String  get()= component.completeName

    private var propertyParameter : KProperty<*>? = null
    val property: KProperty<*> get() = propertyParameter.getOrOperationsEx("Property not yet initialized")
    val propertyRecord: PropertyRecord<F_DTO> get() = PropertyRecord.create(property.castOrInitEx<KProperty<F_DTO>>())

    private var foreignDTOParameter: CommonDTO<F_DTO, FD, FE>? = null
    protected val foreignDTO: CommonDTO<F_DTO, FD, FE>
        get() = foreignDTOParameter.getOrOperationsEx("Foreign dto is not yet assigned")

    var typeParameter : PropertyMapperRecord<FE>? = null
    val type : PropertyMapperRecord<FE>
        get() = typeParameter.getOrInitEx()

    init {
        foreignClass.notifier.subscribe(this, DTOBase.DTOClassEvents.ON_INITIALIZED){parentDto->
            typeParameter = parentDto.entityType
            foreignClass.config.dtoFactory.notificator.subscribe(this, DTOFactory.FactoryEvents.ON_INITIALIZED){parentDto->
                foreignDTOParameter = parentDto
                sourceProvided(parentDto.toDto())
            }
        }
    }

    private fun propertyProvided(property: KProperty<*>){
        if(propertyParameter == null){
            propertyParameter = property
            hostingDTO.bindingHub.setComplexBinding(this.castOrInitEx())
        }
    }
    protected abstract fun sourceProvided(dto: F_DTO)

    operator fun provideDelegate(thisRef: DTO, property: KProperty<*>): ComplexDelegate<DTO, D, E, F_DTO, FD, FE> {
        propertyProvided(property)
        return this
    }
    operator fun getValue(thisRef: DTO, property: KProperty<*>): F_DTO{
        propertyProvided(property)
        return  foreignDTO.toDto()
    }
}


class AttachedForeign<DTO, D, E, F_DTO, FD, FE>(
    hostingDTO: CommonDTO<DTO, D, E>,
    val attachedClass: DTOBase<F_DTO, FD, FE>,
    val foreignDTOProvider: D.(F_DTO)-> Unit,
): ComplexDelegate<DTO, D, E, F_DTO, FD, FE>(hostingDTO, attachedClass, componentInstance(ComponentType.AttachedDelegate, hostingDTO.dtoType))

    where D: DataModel, E: LongEntity, DTO: ModelDTO,
          F_DTO: ModelDTO, FD: DataModel, FE: LongEntity
{

    override fun sourceProvided(dto:F_DTO) {
        hostingDTO.logger.info("Received update from ${foreignClass.completeName}")
        println(dto)
        foreignDTOProvider.invoke(hostingDTO.dataModel, dto)
    }
}

class ParentDelegate<DTO, D, ENTITY, F_DTO, FD, FE>(
    hostingDTO: CommonDTO<DTO, D, ENTITY>,
    foreignClass: DTOBase<F_DTO, FD, FE>,
    val foreignDTOProvider: (D.(F_DTO)-> Unit)?
): ComplexDelegate<DTO, D, ENTITY, F_DTO, FD, FE>(hostingDTO, foreignClass, componentInstance(ComponentType.ParentDelegate, hostingDTO.dtoType))
        where D: DataModel, ENTITY: LongEntity, DTO : ModelDTO, F_DTO: ModelDTO, FD : DataModel, FE: LongEntity
{

    override fun sourceProvided(dto: F_DTO) {
        foreignDTOProvider?.invoke(hostingDTO.dataModel, dto)
    }

}



package po.exposify.dto.components.bindings.property_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.enums.DTOClassStatus
import po.exposify.dto.helpers.toDto
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.Component
import po.exposify.dto.models.ComponentType
import po.exposify.dto.models.SourceObject
import po.exposify.dto.models.componentInstance
import po.exposify.extensions.castOrInitEx
import po.exposify.extensions.getOrOperationsEx
import po.lognotify.TasksManaged
import po.misc.interfaces.Identifiable
import po.misc.reflection.properties.mappers.models.PropertyRecord
import po.misc.types.TypeRecord
import po.misc.types.safeCast
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1


sealed class ComplexDelegate<DTO, D, E, F_DTO, FD,  FE>(
    internal val hostingDTO : CommonDTO<DTO, D, E>,
    internal val foreignClass: DTOBase<F_DTO, FD, FE>,
    internal val component: Component<DTO>
): TasksManaged, Identifiable
        where D: DataModel, E: LongEntity, DTO : ModelDTO,
              F_DTO: ModelDTO, FD : DataModel,  FE: LongEntity
{

    protected val ownDTOClass: DTOBase<DTO, D, E> get() = hostingDTO.dtoClass
    override val componentName: String get()= component.componentName
    override val completeName: String  get()= component.completeName

    protected val typeRecord: TypeRecord<CommonDTO<F_DTO, FD, FE>>?
            = foreignClass.config.registry.getRecord<CommonDTO<F_DTO, FD, FE>>(SourceObject.CommonDTOType)

    private var propertyParameter : KProperty<*>? = null
    val property: KProperty<*> get() = propertyParameter.getOrOperationsEx("Property not yet initialized")
    val propertyRecord: PropertyRecord<F_DTO> get() = PropertyRecord.create(property.castOrInitEx<KProperty<F_DTO>>())

    protected var foreignDTOParameter: CommonDTO<F_DTO, FD, FE>? = null
    val foreignDTO: CommonDTO<F_DTO, FD, FE>
        get() = foreignDTOParameter.getOrOperationsEx("Foreign dto is not yet assigned")

   internal abstract fun resolveForeign():CommonDTO<F_DTO, FD, FE>?

    private fun propertyProvided(property: KProperty<*>){
        if(propertyParameter == null){
            propertyParameter = property
            hostingDTO.bindingHub.setComplexBinding(this.castOrInitEx())
        }
    }
    operator fun provideDelegate(thisRef: DTO, property: KProperty<*>): ComplexDelegate<DTO, D, E, F_DTO, FD, FE> {
        propertyProvided(property)
        return this
    }
    operator fun getValue(thisRef: DTO, property: KProperty<*>): F_DTO{
        propertyProvided(property)
        return  foreignDTO.toDto(foreignClass)
    }
}


class AttachedForeign<DTO, D, E, F_DTO, FD, FE>(
    hostingDTO: CommonDTO<DTO, D, E>,
    foreignClass: DTOBase<F_DTO, FD, FE>,
    val dataIdProperty: KProperty1<D, Long>,
    val foreignDTOProvider: D.(F_DTO)-> Unit,
): ComplexDelegate<DTO, D, E, F_DTO, FD, FE>(hostingDTO, foreignClass, componentInstance(ComponentType.AttachedForeignDelegate, hostingDTO))

    where D: DataModel, E: LongEntity, DTO: ModelDTO,
          F_DTO: ModelDTO, FD: DataModel, FE: LongEntity
{
    init {
        if(ownDTOClass.status == DTOClassStatus.Live){
            resolveForeign()
        }
    }

   override fun resolveForeign():CommonDTO<F_DTO, FD, FE>?{
        val foreignId : Long = dataIdProperty.get(hostingDTO.dataModel)
        val foreignDTO = foreignClass.lookupDTO(foreignId)
        if(foreignDTO != null){
            foreignDTOParameter = foreignDTO
            foreignDTOProvider.invoke(hostingDTO.dataModel, foreignDTO.toDto(foreignClass))
        }else{
            hostingDTO.logger.warn("AttachedForeign dto lookup failure. No DTO with id:${foreignId}")
        }
        return foreignDTO
    }

}

class ParentDelegate<DTO, D, ENTITY, F_DTO, FD, FE>(
    hostingDTO: CommonDTO<DTO, D, ENTITY>,
    foreignClass: DTOBase<F_DTO, FD, FE>,
    val parentDTOProvider: D.(F_DTO)-> Unit
): ComplexDelegate<DTO, D, ENTITY, F_DTO, FD, FE>(hostingDTO, foreignClass, componentInstance(ComponentType.ParentDelegate, hostingDTO))
        where D: DataModel, ENTITY: LongEntity, DTO : ModelDTO, F_DTO: ModelDTO, FD : DataModel, FE: LongEntity
{

    init {
        hostingDTO.subscribe(this, CommonDTO.Events.OnParentAttached){dto->
            if(typeRecord != null){
                dto.safeCast<CommonDTO<F_DTO, FD, FE>>(typeRecord.clazz)?.let { castedDTO ->
                    foreignDTOParameter = castedDTO
                    resolveForeign()
                }?:run { hostingDTO.logger.warn("Safe cast failed for ${dto.completeName}") }
            }else{
                hostingDTO.logger.warn("TypeRecord not received. Won't cast ${dto.completeName}")
            }
        }
    }

    override fun resolveForeign():CommonDTO<F_DTO, FD, FE>?{
        parentDTOProvider.invoke(hostingDTO.dataModel, foreignDTO.toDto(foreignClass))
        return foreignDTO
    }

}



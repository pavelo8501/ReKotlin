package po.exposify.dto.components.bindings.property_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.components.bindings.DelegateStatus
import po.exposify.dto.components.bindings.interfaces.DelegateInterface
import po.exposify.dto.enums.DTOClassStatus
import po.exposify.dto.enums.Delegates
import po.exposify.dto.helpers.toDto
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.castOrInitEx
import po.exposify.extensions.castOrOperationsEx
import po.exposify.extensions.getOrOperationsEx
import po.lognotify.TasksManaged
import po.misc.interfaces.IdentifiableModule
import po.misc.interfaces.IdentifiableModuleInstance
import po.misc.interfaces.asIdentifiableModule
import po.misc.reflection.mappers.models.PropertyRecord
import po.misc.types.TypeRecord
import po.misc.types.safeCast
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1


sealed class ComplexDelegate<DTO, D, E, F_DTO, FD,  FE>(
    internal val hostingDTO : CommonDTO<DTO, D, E>
): TasksManaged, DelegateInterface<DTO, F_DTO>
        where D: DataModel, E: LongEntity, DTO : ModelDTO,
              F_DTO: ModelDTO, FD : DataModel,  FE: LongEntity
{

    override var status: DelegateStatus = DelegateStatus.Created

    override val hostingClass: DTOBase<DTO, *, *>
        get() = hostingDTO.dtoClass

    abstract override val module : IdentifiableModule

    abstract override val foreignClass: DTOBase<F_DTO, FD, FE>
    abstract val typeRecord: TypeRecord<CommonDTO<F_DTO, FD, FE>>?

    protected val ownDTOClass: DTOBase<DTO, D, E> get() = hostingDTO.dtoClass


//    protected val typeRecord: TypeRecord<CommonDTO<F_DTO, FD, FE>>?
//        get() = foreignClass.config.registry.getRecord<CommonDTO<F_DTO, FD, FE>>(SourceObject.CommonDTOType)

    private var propertyParameter : KProperty<F_DTO>? = null
    val property: KProperty<F_DTO> get() = propertyParameter.getOrOperationsEx("Property not yet initialized")
    val propertyRecord: PropertyRecord<F_DTO> get() = PropertyRecord.create(property.castOrInitEx<KProperty<F_DTO>>())

    protected var foreignDTOParameter: CommonDTO<F_DTO, FD, FE>? = null
    val foreignDTO: CommonDTO<F_DTO, FD, FE>
        get() = foreignDTOParameter.getOrOperationsEx("Foreign dto is not yet assigned")

    protected abstract fun propertyResolved(property: KProperty<F_DTO>)

   internal abstract fun resolveForeign():CommonDTO<F_DTO, FD, FE>?

    private fun propertyProvided(property: KProperty<*>){
        if(propertyParameter == null){
            val castedProperty :KProperty<F_DTO>? = property.safeCast<KProperty<F_DTO>>()
            castedProperty?.let {
                propertyParameter = it
                propertyResolved(it)

            }?:println("Unable to cast KProperty<*> to KProperty<F_DTO>")
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


class AttachedForeignDelegate<DTO, D, E, F_DTO, FD, FE>(
    hostingDTO: CommonDTO<DTO, D, E>,
    override val foreignClass: DTOBase<F_DTO, FD, FE>,
    val dataIdProperty: KProperty1<D, Long>,
    val foreignDTOCallback: D.(F_DTO)-> Unit,
): ComplexDelegate<DTO, D, E, F_DTO, FD, FE>(hostingDTO)
    where D: DataModel, E: LongEntity, DTO: ModelDTO,
          F_DTO: ModelDTO, FD: DataModel, FE: LongEntity
{


    override val module: IdentifiableModule = asIdentifiableModule(hostingDTO.sourceName, "AttachedForeignDelegate",
        Delegates.AttachedForeignDelegate)



    override val typeRecord: TypeRecord<CommonDTO<F_DTO, FD, FE>>? = null

    init {

        if(ownDTOClass.status == DTOClassStatus.Live){
            resolveForeign()
        }
    }

    override fun propertyResolved(property: KProperty<F_DTO>){
        hostingDTO.bindingHub.setAttachedForeignDelegate(this.castOrOperationsEx())
    }

   override fun resolveForeign():CommonDTO<F_DTO, FD, FE>?{
        val foreignId : Long = dataIdProperty.get(hostingDTO.dataModel)
        val foreignDTO = foreignClass.lookupDTO(foreignId)
        if(foreignDTO != null){
            foreignDTOParameter = foreignDTO
            foreignDTOCallback.invoke(hostingDTO.dataModel, foreignDTO.toDto(foreignClass))
        }else{
            hostingDTO.logger.warn("AttachedForeign dto lookup failure. No DTO with id:${foreignId}")
        }
        return foreignDTO
    }

}

class ParentDelegate<DTO, D, ENTITY, F_DTO, FD, FE>(
    hostingDTO: CommonDTO<DTO, D, ENTITY>,
    override val foreignClass: DTOBase<F_DTO, FD, FE>,
    val parentDTOProvider: D.(F_DTO)-> Unit
): ComplexDelegate<DTO, D, ENTITY, F_DTO, FD, FE>(hostingDTO)
        where D: DataModel, ENTITY: LongEntity, DTO : ModelDTO, F_DTO: ModelDTO, FD : DataModel, FE: LongEntity
{

    override val module: IdentifiableModule = asIdentifiableModule(hostingDTO.sourceName, "AttachedForeignDelegate",
        Delegates.ParentDelegate)

    override val typeRecord: TypeRecord<CommonDTO<F_DTO, FD, FE>>? = null


    init {
        hostingDTO.subscribe(module, CommonDTO.Events.OnParentAttached){dto->
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


    override fun propertyResolved(property: KProperty<F_DTO>){
        hostingDTO.bindingHub.setParentDelegate(this.castOrOperationsEx())
    }

    override fun resolveForeign():CommonDTO<F_DTO, FD, FE>?{
        parentDTOProvider.invoke(hostingDTO.dataModel, foreignDTO.toDto(foreignClass))
        return foreignDTO
    }

}



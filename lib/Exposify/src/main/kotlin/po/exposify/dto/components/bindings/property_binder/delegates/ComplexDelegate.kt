package po.exposify.dto.components.bindings.property_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.EntityID
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.RootDTO
import po.exposify.dto.components.bindings.DelegateStatus
import po.exposify.dto.components.bindings.interfaces.DelegateInterface
import po.exposify.dto.components.bindings.interfaces.ForeignDelegateInterface
import po.exposify.dto.enums.DTOClassStatus
import po.exposify.dto.helpers.toDto
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.castOrInitEx
import po.exposify.extensions.castOrOperationsEx
import po.exposify.extensions.getOrOperationsEx
import po.misc.data.SmartLazy
import po.misc.interfaces.IdentifiableClass
import po.misc.interfaces.asIdentifiableClass
import po.misc.types.safeCast
import kotlin.getValue
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

sealed class ComplexDelegate<DTO, D, E, F_DTO, FD,  FE>(
    val whoAmI: WhoAmI,
    internal val hostingDTO : CommonDTO<DTO, D, E>
): DelegateInterface<DTO, F_DTO>, ForeignDelegateInterface
    where D: DataModel, E: LongEntity, DTO : ModelDTO, F_DTO: ModelDTO, FD : DataModel,  FE: LongEntity
{
    enum class WhoAmI{
        AttachedForeignDelegate,
        ParentDelegate
    }

    override var status: DelegateStatus = DelegateStatus.Created

    override val hostingClass: DTOBase<DTO, D, E>
        get() = hostingDTO.dtoClass

    abstract override val foreignClass: DTOBase<F_DTO, FD, FE>

    protected val ownDTOClass: DTOBase<DTO, D, E> get() = hostingDTO.dtoClass
    private var propertyParameter : KProperty<F_DTO>? = null
    val property: KProperty<F_DTO> get() = propertyParameter.getOrOperationsEx("Property not yet initialized")

    val name: String by SmartLazy("Uninitialized"){
        propertyParameter?.name
    }

    protected var foreignDTOParameter: CommonDTO<F_DTO, FD, FE>? = null
    val foreignDTO: CommonDTO<F_DTO, FD, FE>
        get() {
          return  foreignDTOParameter.getOrOperationsEx("Foreign dto is not yet assigned @ ${whoAmI.name}")
        }

   override fun resolveProperty(property: KProperty<*>){
        if(propertyParameter == null){
            propertyParameter = property.castOrInitEx("Unable to cast KProperty<*> to KProperty<F_DTO>")
            when(this){
                is ParentDelegate ->{
                    hostingDTO.bindingHub.setParentDelegate(this)
                }
                is AttachedForeignDelegate ->{
                    hostingDTO.bindingHub.setAttachedForeignDelegate(this)
                }
            }
        }
    }
    override fun updateStatus(status: DelegateStatus) {
        this.status = status
    }

    operator fun provideDelegate(thisRef: DTO, property: KProperty<*>): ComplexDelegate<DTO, D, E, F_DTO, FD, FE> {
        resolveProperty(property)
        return this
    }
    operator fun getValue(thisRef: DTO, property: KProperty<*>): F_DTO{
        resolveProperty(property)
        return  foreignDTO.toDto(foreignClass)
    }
}


class AttachedForeignDelegate<DTO, D, E, F_DTO, FD, FE>(
    hostingDTO: CommonDTO<DTO, D, E>,
    override val foreignClass: RootDTO<F_DTO, FD, FE>,
    val dataIdProperty: KMutableProperty1<D, Long>,
    val entityIdProperty: KMutableProperty1<E,  Long>,
    val foreignDTOCallback: (F_DTO)-> Unit,
): ComplexDelegate<DTO, D, E, F_DTO, FD, FE>(WhoAmI.AttachedForeignDelegate, hostingDTO), IdentifiableClass
    where D: DataModel, E: LongEntity, DTO: ModelDTO,
          F_DTO: ModelDTO, FD: DataModel, FE: LongEntity
{

    override val identity = asIdentifiableClass("DTOFactory", hostingDTO.sourceName)

    val attachedName: String get() = entityIdProperty.name

    private fun getForeignDTOAndResolve(id: Long){
        val pickResult = foreignClass.serviceContext.pickById(id)
        pickResult.getDTO()?.let { foreignDTO ->
            foreignDTOParameter = pickResult.getAsCommonDTOForced()

            foreignDTOCallback.invoke(foreignDTO)
        }?:run {
            hostingDTO.logger.warn("AttachedForeign dto lookup failure. No DTO with id:${id}")
        }
    }


   fun resolveForeign(data:D, entity:E?){
       val foreignId : Long = dataIdProperty.get(data)
       if(entity !=  null){
           entityIdProperty.set(entity, foreignId)
       }
       getForeignDTOAndResolve(foreignId)
    }

    fun resolveForeign(entity:E){
        val foreignId : Long = entityIdProperty.get(entity)
        dataIdProperty.set(hostingDTO.dataModel, foreignId)
        getForeignDTOAndResolve(foreignId)
    }
}

class ParentDelegate<DTO, D, E, F_DTO, FD, FE>(
    hostingDTO: CommonDTO<DTO, D, E>,
    override val foreignClass: DTOBase<F_DTO, FD, FE>,
    val parentDTOProvider: D.(F_DTO)-> Unit
): ComplexDelegate<DTO, D, E, F_DTO, FD, FE>(WhoAmI.ParentDelegate, hostingDTO), IdentifiableClass
        where D: DataModel, E: LongEntity, DTO : ModelDTO,
              F_DTO: ModelDTO, FD : DataModel, FE: LongEntity
{

    override val identity = asIdentifiableClass("ParentDelegate", hostingDTO.sourceName)

    fun resolveForeign(dto: CommonDTO<*, *, *>){
        foreignDTOParameter = dto.safeCast()
        val asDTO = foreignDTO.toDto(foreignClass)
        parentDTOProvider.invoke(hostingDTO.dataModel, asDTO)
    }
}



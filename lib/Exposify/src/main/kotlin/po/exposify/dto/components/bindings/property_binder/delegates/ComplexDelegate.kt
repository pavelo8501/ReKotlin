package po.exposify.dto.components.bindings.property_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.RootDTO
import po.exposify.dto.components.bindings.BindingHub
import po.exposify.dto.components.bindings.DelegateStatus
import po.exposify.dto.components.bindings.interfaces.DelegateInterface
import po.exposify.dto.components.bindings.interfaces.ForeignDelegateInterface
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.CommonDTOType
import po.exposify.extensions.castOrInit
import po.exposify.extensions.getOrOperations
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asSubIdentity
import po.misc.data.SmartLazy
import po.misc.types.TypeData
import po.misc.types.containers.updatable.ActionValue
import kotlin.getValue
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

sealed class ComplexDelegate<DTO, D, E, F, FD,  FE>(
    internal val hostingDTO : CommonDTO<DTO, D, E>,
): DelegateInterface<DTO, F>, ForeignDelegateInterface, CTX
    where D: DataModel, E: LongEntity, DTO : ModelDTO, F: ModelDTO, FD : DataModel,  FE: LongEntity
{

    override var status: DelegateStatus = DelegateStatus.Created
    override val hostingClass: DTOBase<DTO, D, E>
        get() = hostingDTO.dtoClass


    private var propertyParameter : KProperty<F>? = null
    val property: KProperty<F> get() = propertyParameter.getOrOperations(this)
    val name: String by SmartLazy("Uninitialized"){
        propertyParameter?.name
    }

    abstract val foreignClass: DTOBase<F, FD, FE>
    protected val foreignInitialized: Boolean get() = foreignDTOBacking != null

    protected var foreignCommonBacking: CommonDTO<F, FD, FE>? = null
    val foreignCommon: CommonDTO<F, FD, FE>  get() = foreignCommonBacking.getOrOperations("foreignDTO Backing property", this)

    protected var foreignDTOBacking: F? = null
    val foreignDTO: F get() = foreignDTOBacking.getOrOperations("ForeignDTO Backing", this)


   override fun resolveProperty(property: KProperty<*>){
        if(propertyParameter == null){
            propertyParameter = property.castOrInit<KProperty<F>>(this)
            when(this){
                is ParentDelegate ->{
                    hostingDTO.hub.registerParentDelegate(this)
                }
                is AttachedForeignDelegate ->{
                    hostingDTO.hub.registerAttachedForeignDelegate(this)
                }
            }
        }
    }
   override fun updateStatus(status: DelegateStatus) {
        this.status = status
    }

    operator fun provideDelegate(thisRef: DTO, property: KProperty<*>): ComplexDelegate<DTO, D, E, F, FD, FE> {
        resolveProperty(property)
        return this
    }
    operator fun getValue(thisRef: DTO, property: KProperty<*>): F{
        resolveProperty(property)
        return  foreignDTO
    }
}


class AttachedForeignDelegate<DTO, D, E, F, FD, FE>(
    hostingDTO: CommonDTO<DTO, D, E>,
    override val foreignClass: RootDTO<F, FD, FE>,
    val dataIdProperty: KMutableProperty1<D, Long>,
    val entityIdProperty: KMutableProperty1<E,  Long>,
    val foreignDTOCallback: (F)-> Unit
): ComplexDelegate<DTO, D, E, F, FD, FE>(hostingDTO)
    where D: DataModel, E: LongEntity, DTO: ModelDTO, F: ModelDTO, FD: DataModel, FE: LongEntity
{
    override val identity: CTXIdentity<out CTX> = asSubIdentity(this, hostingDTO)

        val attachedName: String get() = entityIdProperty.name

    private fun getForeignDTO(id: Long): F{
        val result = foreignClass.executionContext.pickById(id)
        foreignDTOBacking = result.getDTOForced()
        foreignDTOCallback.invoke(foreignDTO)
        return foreignDTO
    }

   fun resolveForeign(data:D):D{
       val foreignId : Long = dataIdProperty.get(data)
      val dto =  if(!foreignInitialized){
           getForeignDTO(foreignId)
       }else{
           foreignDTO
       }
       return data
    }

    internal fun update(){

        dataIdProperty.set(hostingDTO.dataContainer.source, foreignDTO.id)
    }

    internal fun resolveForeign(entity:E):E{
        val foreignId : Long = entityIdProperty.get(entity)
        val dto =  if(!foreignInitialized){
            getForeignDTO(foreignId)
        }else{
            foreignDTO
        }
        return entity
    }
    internal fun update(entity:E){
        entityIdProperty.set(entity, foreignDTO.id)
    }
}


class ParentDelegate<DTO, D, E, F, FD, FE>(
    hostingDTO: CommonDTO<DTO, D, E>,
    override val foreignClass: DTOBase<F, FD, FE>,
    val parentDTOProvider: D.(F)-> Unit
): ComplexDelegate<DTO, D, E, F, FD, FE>(hostingDTO)
        where DTO : ModelDTO, D: DataModel, E: LongEntity,  F: ModelDTO, FD : DataModel, FE: LongEntity
{

    override val identity: CTXIdentity<out CTX> = asSubIdentity(this, hostingDTO)

    val initialized: Boolean get() = entityBinderBacking != null
    val hub: BindingHub<DTO, D, E> = hostingDTO.hub

    val foreignDTOType: TypeData<F> = foreignClass.dtoType
    //val commonType: CommonDTOType<F, FD, FE> get() = foreignClass.commonType

    internal var entityBinderBacking: ActionValue<E>? = null
    val entityBinder: ActionValue<E> by lazy { entityBinderBacking.getOrOperations(this) }

    fun assignEntityBinder(binder: ActionValue<E>){
        entityBinderBacking = binder
    }
    fun resolve(entity:E) {
        entityBinder.provideValue(entity)
    }

    fun assignParentDTO(dto: F){
        foreignDTOBacking = dto

        hostingDTO.notifier.subscribe<CommonDTO<DTO, D, E>>(this, CommonDTO.DTOEvents.OnDTOComplete){
            val commonDTO = it.getData()
            val dataModel =  commonDTO.dataContainer.source
            parentDTOProvider.invoke(dataModel, dto)
        }

        if(hostingDTO.dataContainer.isSourceAvailable){
            parentDTOProvider.invoke(hostingDTO.dataContainer.source, dto)
        }
    }
}




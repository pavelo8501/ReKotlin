package po.exposify.dto.components.bindings.relation_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.RootDTO
import po.exposify.dto.components.bindings.DelegateStatus
import po.exposify.dto.helpers.asDTO
import po.exposify.dto.helpers.warning
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.CommonDTOType
import po.exposify.extensions.castOrInit
import po.exposify.extensions.castOrOperations
import po.exposify.extensions.getOrOperations
import po.lognotify.TasksManaged
import po.misc.containers.LazyContainer
import po.misc.containers.lazyContainerOf
import po.misc.context.CTX
import po.misc.context.asSubIdentity
import po.misc.data.SmartLazy
import po.misc.data.helpers.output
import po.misc.data.styles.Colour
import kotlin.getValue
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty


sealed class ComplexDelegate<DTO, D, E, F, FD, FE>(
    val dtoBase: DTOBase<F, FD, FE>,
    internal val hostingDTO: CommonDTO<DTO, D, E>,
) : TasksManaged where D : DataModel, E : LongEntity, DTO : ModelDTO, F : ModelDTO, FD : DataModel, FE : LongEntity
{
    var status: DelegateStatus = DelegateStatus.Created

    val commonDTOType: CommonDTOType<F, FD, FE> get() = dtoBase.commonDTOType

    private var propertyParameter: KProperty<DTO>? = null
    val property: KProperty<DTO> get() = propertyParameter.getOrOperations(this)
    val name: String by SmartLazy("Uninitialized") {
        propertyParameter?.name
    }

    protected open val commonDTOContainer : LazyContainer<CommonDTO<F, FD, FE>> = lazyContainerOf<CommonDTO<F, FD, FE>>()



    protected abstract fun beforeRegistered()

    fun resolveProperty(property: KProperty<*>) {
        propertyParameter = property.castOrInit<KProperty<DTO>>(this)


        beforeRegistered()
        hostingDTO.bindingHub.registerComplexDelegate(this)
    }

    fun updateStatus(status: DelegateStatus) {
        this.status = status
    }

    operator fun provideDelegate(
        thisRef: DTO,
        property: KProperty<*>,
    ): ComplexDelegate<DTO, D, E, F, FD, FE> {
        resolveProperty(property)
        return this
    }

    operator fun getValue(
        thisRef: DTO,
        property: KProperty<*>,
    ): F {
        return commonDTOContainer.getValue(this).asDTO()
    }
}



class AttachedForeignDelegate<DTO, D, E, F, FD, FE>(
    hostingDTO: CommonDTO<DTO, D, E>,
    val dtoClass: RootDTO<F, FD, FE>,
    val dataIdProperty: KMutableProperty1<D, Long>,
    val entityIdProperty: KMutableProperty1<E, Long>,
    val foreignDTOCallback: ((F) -> Unit)?
) : ComplexDelegate<DTO, D, E, F, FD, FE>(dtoClass, hostingDTO)
    where D : DataModel, E : LongEntity, DTO : ModelDTO, F : ModelDTO, FD : DataModel, FE : LongEntity
{
     override val identity = asSubIdentity(this, hostingDTO)

     val attachedName: String get() = entityIdProperty.name

    fun resolve(commonDTO : CommonDTO<F, FD, FE>){
        commonDTOContainer.provideValue(commonDTO)
        val dataModel = hostingDTO.dataContainer.getValue(this)
        dataIdProperty.set(dataModel, 1)
    }

    fun resolve(dataModel : D) {
        val lookupId = dataIdProperty.get(dataModel)
        dtoClass.executionContext.dtoLookupInExistent(lookupId)?.let {
            commonDTOContainer.provideValue(it)
        }
    }

    fun resolve(entity : E) {
        val lookupId = entityIdProperty.get(entity)
        dtoClass.executionContext.dtoLookupInExistent(lookupId)?.let {dto->
             val casted = dto.castOrOperations<CommonDTO<F, FD, FE>>(this)
             commonDTOContainer.provideValue(casted)
        }
    }

    fun updateEntity(callingContext: CTX,  entity: E) {
        entityIdProperty.set(entity, commonDTOContainer.getValue(this).id)
    }

    fun updateDataModel(callingContext: CTX) {
        val dataModel =  hostingDTO.dataContainer.getValue(this)
        val resolvedDTO =  commonDTOContainer.getValue(this)
        dataIdProperty.set(dataModel, resolvedDTO.id)
    }


    override fun beforeRegistered() {
       // identity.setNamePattern { "AttachedForeignDelegate[${property.name}]" }
    }
}

class ParentDelegate<DTO, D, E, F, FD, FE>(
    hostingDTO: CommonDTO<DTO, D, E>,
    val dtoClass: DTOBase<F, FD, FE>,
    val entityProperty: KMutableProperty1<E, FE>,
    val parentDTOProvider: D.(F) -> Unit,
) : ComplexDelegate<DTO, D, E, F, FD, FE>(dtoClass, hostingDTO)
    where DTO : ModelDTO, D : DataModel, E : LongEntity, F : ModelDTO, FD : DataModel, FE : LongEntity {

    override val identity  = asSubIdentity(this, hostingDTO)


//    override val commonDTOContainer: LazyContainer<CommonDTO<F, FD, FE>>
//        get() = hostingDTO.parentDTOContainer.castOrOperations(this)
//

    fun resolve(commonDTO : CommonDTO<F, FD, FE>){
        hostingDTO.parentDTOContainer.provideValue(commonDTO)

        commonDTOContainer.provideValue(commonDTO)
        val dataModel = hostingDTO.dataContainer.getValue(this)
        parentDTOProvider.invoke(dataModel, commonDTO.asDTO())
    }

    fun updateEntity(callingContext: CTX,  entity: E){
        if(commonDTOContainer.value == null){
            warning("$identifiedByName commonDTO not resolved. updateEntity skipped")
        }else {
            val commonDTO = commonDTOContainer.getValue(callingContext)
            val parentEntity = commonDTO.entityContainer.getValue(this)
            entityProperty.set(entity, parentEntity)
        }
    }

    override fun beforeRegistered() {

    }

    fun bindEntity(callingContext: CTX, rootEntity: E, childEntity:FE) {
        //entityProperty.set(childEntity, rootEntity)
    }
}

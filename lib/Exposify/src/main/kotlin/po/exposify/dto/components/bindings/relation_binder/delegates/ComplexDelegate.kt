package po.exposify.dto.components.bindings.relation_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.RootDTO
import po.exposify.dto.components.bindings.DelegateStatus
import po.exposify.dto.components.bindings.interfaces.DelegateInterface
import po.exposify.dto.components.bindings.interfaces.ForeignDelegateInterface
import po.exposify.dto.helpers.asDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.CommonDTOType
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.castOrInit
import po.exposify.extensions.castOrOperations
import po.exposify.extensions.getOrOperations
import po.lognotify.TasksManaged
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asSubIdentity
import po.misc.data.SmartLazy
import po.misc.types.containers.updatable.ActionValue
import kotlin.getValue
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

sealed class ComplexDelegate<DTO, D, E, F, FD, FE>(
    internal val hostingDTO: CommonDTO<DTO, D, E>,
) : DelegateInterface<DTO, D, E>, ForeignDelegateInterface, TasksManaged
    where D : DataModel, E : LongEntity, DTO : ModelDTO, F : ModelDTO, FD : DataModel, FE : LongEntity {

    override var status: DelegateStatus = DelegateStatus.Created
    override val hostingClass: DTOBase<DTO, D, E>
        get() = hostingDTO.dtoClass

    private var propertyParameter: KProperty<F>? = null
    val property: KProperty<F> get() = propertyParameter.getOrOperations(this)
    val name: String by SmartLazy("Uninitialized") {
        propertyParameter?.name
    }

    abstract val foreignClass: DTOBase<F, FD, FE>
    protected val foreignInitialized: Boolean get() = foreignCommonBacking != null
    val foreignCommonDTOType: CommonDTOType<F, FD, FE> get() = foreignClass.commonDTOType

    private var foreignCommonBacking: CommonDTO<F, FD, FE>? = null
    val foreignCommon: CommonDTO<F, FD, FE> get() = foreignCommonBacking.getOrOperations(this)

    val foreignDTO: F get() = foreignCommonBacking.getOrOperations(this).asDTO()

    protected abstract fun onPropertyResolved()

    protected abstract fun beforeRegistered()

    protected fun provideForeignDTO(commonDTO: CommonDTO<F, FD, FE>): CommonDTO<F, FD, FE> {
        foreignCommonBacking = commonDTO
        updateStatus(DelegateStatus.Initialized)
        return foreignCommon
    }

    override fun resolveProperty(property: KProperty<*>) {
        if (propertyParameter == null) {
            propertyParameter = property.castOrInit<KProperty<F>>(this)
            beforeRegistered()
            hostingDTO.bindingHub.registerComplexDelegate(this)
            onPropertyResolved()
        }
    }

    override fun updateStatus(status: DelegateStatus) {
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
        resolveProperty(property)
        return foreignDTO
    }
}

class AttachedForeignDelegate<DTO, D, E, F, FD, FE>(
    hostingDTO: CommonDTO<DTO, D, E>,
    override val foreignClass: RootDTO<F, FD, FE>,
    val dataIdProperty: KMutableProperty1<D, Long>,
    val entityIdProperty: KMutableProperty1<E, Long>,
    val foreignDTOCallback: (F) -> Unit,
) : ComplexDelegate<DTO, D, E, F, FD, FE>(hostingDTO)
    where D : DataModel, E : LongEntity, DTO : ModelDTO, F : ModelDTO, FD : DataModel, FE : LongEntity {
    override val identity: CTXIdentity<AttachedForeignDelegate<DTO, D, E, F, FD, FE>> = asSubIdentity(this, hostingDTO)

    val attachedName: String get() = entityIdProperty.name

    private fun getForeignDTO(id: Long): CommonDTO<F, FD, FE> {
        val result = foreignClass.executionContext.pickById(id)
        return result.commonDTO?.let {
            val foreignDTO = provideForeignDTO(it.castOrOperations(this))
            foreignDTOCallback.invoke(foreignDTO.asDTO())
            foreignDTO
        } ?: throw result.failureCause ?: OperationsException("Result unavailable", ExceptionCode.CAST_FAILURE, this)
    }

    fun resolveForeign(data: D): D {
        val foreignId: Long = dataIdProperty.get(data)
        val dto =
            if (!foreignInitialized) {
                getForeignDTO(foreignId)
            } else {
                foreignDTO
            }
        return data
    }

    internal fun update() {
        dataIdProperty.set(hostingDTO.dataContainer.getValue(this), foreignDTO.id)
    }

    internal fun resolveForeign(entity: E): E {
        val foreignId: Long = entityIdProperty.get(entity)
        val dto =
            if (!foreignInitialized) {
                getForeignDTO(foreignId)
            } else {
                foreignDTO
            }
        return entity
    }

    internal fun update(entity: E) {
        entityIdProperty.set(entity, foreignDTO.id)
    }

    override fun onPropertyResolved() {
        identity.setNamePattern { "AttachedForeignDelegate[${property.name}]" }
    }

    override fun beforeRegistered() {
    }
}

class ParentDelegate<DTO, D, E, F, FD, FE>(
    hostingDTO: CommonDTO<DTO, D, E>,
    override val foreignClass: DTOBase<F, FD, FE>,
    val entityProperty: KMutableProperty1<E, FE>,
    val parentDTOProvider: D.(F) -> Unit,
) : ComplexDelegate<DTO, D, E, F, FD, FE>(hostingDTO)
    where DTO : ModelDTO, D : DataModel, E : LongEntity, F : ModelDTO, FD : DataModel, FE : LongEntity {
    override val identity: CTXIdentity<out CTX> = asSubIdentity(this, hostingDTO)

    val initialized: Boolean get() = entityBinderBacking != null

    internal var entityBinderBacking: ActionValue<E>? = null
    val entityBinder: ActionValue<E> by lazy { entityBinderBacking.getOrOperations(this) }

    override fun onPropertyResolved() {
    }

    override fun beforeRegistered() {
        hostingDTO.parentDTO.requestValueCasting<CommonDTO<F, FD, FE>>(this, foreignClass.commonDTOType.commonType.kClass) { dto ->
            provideForeignDTO(dto)
        }
    }

    fun assignEntityBinder(binder: ActionValue<E>) {
        entityBinderBacking = binder
    }

    fun resolve(entity: E) {
        entityBinder.provideValue(entity)
    }

    fun resolveParent(commonDTO: CommonDTO<F, FD, FE>){
        provideForeignDTO(commonDTO)
        hostingDTO.dataContainer.requestValue(this){
            parentDTOProvider.invoke(it, commonDTO.asDTO())
        }
    }

//    fun assignParentDTO(commonDTO: CommonDTO<F, FD, FE>) {
//        provideForeignDTO(commonDTO)
//       // val entity = commonDTO.entityContainer.value
////        if (entity != null) {
////            entityProperty.set(hostingDTO.entityContainer.value!!, entity)
////        }
//
//        if (hostingDTO.dataContainer.isValueAvailable) {
//            parentDTOProvider.invoke(hostingDTO.dataContainer.getValue(this), foreignDTO)
//        }
//    }
}

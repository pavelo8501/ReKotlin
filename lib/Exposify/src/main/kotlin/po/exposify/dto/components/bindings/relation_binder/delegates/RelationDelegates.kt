package po.exposify.dto.components.bindings.relation_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.SizedIterable
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.components.bindings.DelegateStatus
import po.exposify.dto.components.bindings.helpers.newDTO
import po.exposify.dto.components.bindings.interfaces.DelegateInterface
import po.exposify.dto.components.bindings.interfaces.ForeignDelegateInterface
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.extensions.addTrackerInfo
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.InitException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.initException
import po.exposify.extensions.castOrInit
import po.exposify.extensions.getOrOperations
import po.lognotify.classes.action.InlineAction
import po.lognotify.classes.action.runInlineAction
import po.misc.data.SmartLazy
import po.misc.interfaces.IdentifiableClass
import po.misc.interfaces.asIdentifiableClass
import po.misc.lookups.HierarchyNode
import po.misc.types.TypeRecord
import po.misc.types.castOrThrow
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1


sealed class RelationDelegate<DTO, DATA, ENTITY, F_DTO, FD, FE, V: Any>(
    val hostingDTO : CommonDTO<DTO, DATA, ENTITY>,
    override val foreignClass: DTOClass<F_DTO, FD, FE>,
    val foreignEntityProperty: KMutableProperty1<FE, ENTITY>
): DelegateInterface<DTO, F_DTO>, ForeignDelegateInterface, IdentifiableClass, InlineAction
        where DTO: ModelDTO, DATA: DataModel,  ENTITY : LongEntity,
              F_DTO: ModelDTO, FD: DataModel,  FE: LongEntity
{

    override var status: DelegateStatus = DelegateStatus.Created
    abstract val cardinality : Cardinality

    override val hostingClass: DTOBase<DTO, *, *>
        get() = hostingDTO.dtoClass

    protected var onPropertyInitialized: ((KProperty<*>)-> Unit)? = null
    private var propertyParameter : KProperty<F_DTO>? = null
    val property: KProperty<F_DTO> get() = propertyParameter.getOrOperations(this)
    val name : String by SmartLazy("Uninitialized"){ propertyParameter?.name }
    override val identity = asIdentifiableClass("RelationDelegate", hostingDTO.sourceName)

    protected val dtoType : TypeRecord<DTO> get() =  hostingDTO.dtoType
    protected val childType : TypeRecord<F_DTO> get() = foreignClass.dtoType

    abstract fun getEffectiveValue():V
    abstract fun getChildDTOs(): List<CommonDTO<F_DTO, FD, FE>>
    abstract fun getPropertyData(data:DATA):List<FD>
    abstract fun getEntityData(entity:ENTITY):List<FE>
    protected abstract fun saveDto(dto:CommonDTO<F_DTO, FD, FE>, updateData: Boolean = false)
    protected abstract fun saveDto(dto: CommonDTO<F_DTO, FD, FE>, entity:FE)

    operator fun getValue(thisRef: DTO, property: KProperty<*>): V {
        resolveProperty(property)
        return getEffectiveValue()
    }
    operator fun provideDelegate(thisRef: DTO, property: KProperty<*>): RelationDelegate<DTO, DATA, ENTITY, F_DTO, FD, FE, V> {
        resolveProperty(property)
        return this
    }
    override fun resolveProperty(property: KProperty<*>){
        if(propertyParameter == null){
            propertyParameter = property.castOrInit(this)
            identity.updateSourceName(property.name)
            hostingDTO.bindingHub.setRelationBinding<F_DTO>(this)
            onPropertyInitialized?.invoke(property)
        }
    }

    override fun updateStatus(status: DelegateStatus) {
        this.status = status
    }

    fun updateBy(data:DATA) = runInlineAction("updateByData") {
        val childDtoList = getChildDTOs()
        getPropertyData(data).forEach { data ->
            val existent = childDtoList.find { it.id == data.id }
            if (existent != null) {
                existent.bindingHub.update(data)
            }else{
                val newDto = foreignClass.newDTO(data)
                newDto.addTrackerInfo(CrudOperation.Insert, this)
                val insertedEntity = newDto.daoService.save {
                    newDto.bindingHub.update(it)
                    foreignEntityProperty.set(it, hostingDTO.getEntity())
                }
                newDto.provideEntity(insertedEntity)
                foreignClass.registerDTO(newDto)
                saveDto(newDto)
                newDto.bindingHub.create()
            }
        }
    }

    fun createByEntity(entity:ENTITY) = runInlineAction("selectByEntity") {
        val foreignEntities =  getEntityData(entity)
        foreignEntities.forEach { entity->
            val newDto = foreignClass.config.dtoFactory.createDto()
            newDto.bindingHub.select(entity)
            saveDto(newDto, true)
        }
    }
}


class OneToManyDelegate<DTO, DATA, ENTITY, F_DTO, FD, FE>(
    dto : CommonDTO<DTO, DATA, ENTITY>,
    childModel: DTOClass<F_DTO, FD, FE>,
    val dataProperty: KProperty1<DATA, MutableList<FD>>,
    val entitiesProperty: KProperty1<ENTITY, SizedIterable<FE>>,
    foreignEntityProperty: KMutableProperty1<FE, ENTITY>,
) : RelationDelegate<DTO, DATA, ENTITY, F_DTO, FD, FE, List<F_DTO>>(dto, childModel, foreignEntityProperty)
        where DTO : ModelDTO, DATA : DataModel, ENTITY : LongEntity,
              F_DTO: ModelDTO,  FD : DataModel, FE : LongEntity
{

    override val cardinality : Cardinality = Cardinality.ONE_TO_MANY

    private val dtos : MutableList<CommonDTO<F_DTO, FD, FE>> = mutableListOf()
    override fun getEffectiveValue(): List<F_DTO> {
        return dtos.map {
            it.castOrThrow<F_DTO, InitException>(childType.clazz, this) {msg-> initException(msg, ExceptionCode.CAST_FAILURE) }
        }
    }

    override fun getChildDTOs(): List<CommonDTO<F_DTO, FD, FE>> {
        return dtos.toList()
    }

    override fun getPropertyData(data:DATA): List<FD> {
        return dataProperty.get(data)
    }
    override fun getEntityData(entity: ENTITY): List<FE> {
        return entitiesProperty.get(entity).toList()
    }

    override fun saveDto(dto: CommonDTO<F_DTO, FD, FE>, entity:FE){
        dto.bindingHub.setParent(hostingDTO)
        dto.finalizeCreation(entity, cardinality)
        dtos.add(dto)
    }

    override fun saveDto(dto: CommonDTO<F_DTO, FD, FE>, updateData: Boolean) {
        dto.bindingHub.setParent(hostingDTO)
        dtos.add(dto)
        if(updateData){
           val mutable = dataProperty.get(hostingDTO.dataModel)
            mutable.add(dto.dataModel)
        }
    }
}


class OneToOneDelegate<DTO, DATA, ENTITY, F_DTO, FD, FE>(
    dto : CommonDTO<DTO, DATA, ENTITY>,
    childModel: DTOClass<F_DTO, FD, FE>,
    val dataProperty: KMutableProperty1<DATA, FD>,
    val entityProperty: KMutableProperty1<ENTITY, FE>,
    foreignEntityProperty: KMutableProperty1<FE, ENTITY>,
) : RelationDelegate<DTO, DATA, ENTITY, F_DTO, FD, FE, F_DTO>(dto, childModel, foreignEntityProperty)
        where DTO : ModelDTO, DATA : DataModel, ENTITY : LongEntity,
              F_DTO: ModelDTO,  FD : DataModel, FE : LongEntity {

    override val cardinality : Cardinality = Cardinality.ONE_TO_ONE

    private var childDTO : CommonDTO<F_DTO, FD, FE>? = null
    override fun getEffectiveValue(): F_DTO {
        return childDTO.castOrThrow<F_DTO, InitException>(childType.clazz, this){msg-> initException(msg, ExceptionCode.CAST_FAILURE) }
    }

    override fun getChildDTOs(): List<CommonDTO<F_DTO, FD, FE>> {
       return childDTO?.let { listOf(it) }?:emptyList()
    }
    override fun getPropertyData(data: DATA): List<FD> {
        return listOf(dataProperty.get(data))
    }
    override fun getEntityData(entity: ENTITY): List<FE> {
      return listOf(entityProperty.get(entity))
    }

    override fun saveDto(dto: CommonDTO<F_DTO, FD, FE>, entity:FE) {
        dto.bindingHub.setParent(hostingDTO)
        dto.finalizeCreation(entity, cardinality)
        childDTO = dto
    }

    override fun saveDto(dto: CommonDTO<F_DTO, FD, FE>, updateData: Boolean) {
        dto.bindingHub.setParent(hostingDTO)
        childDTO = dto
        if(updateData){
            dataProperty.set(hostingDTO.dataModel, dto.dataModel)
        }
    }

}
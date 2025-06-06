package po.exposify.dto.components.bindings.relation_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.SizedIterable
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOClass
import po.exposify.dto.components.bindings.helpers.createDTO
import po.exposify.dto.components.bindings.helpers.newDTO
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.extensions.addTrackerInfo
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.Component
import po.exposify.dto.models.ComponentType
import po.exposify.dto.models.SourceObject
import po.exposify.exceptions.InitException
import po.exposify.extensions.castOrInitEx
import po.exposify.extensions.getOrOperationsEx
import po.lognotify.TasksManaged
import po.misc.interfaces.Identifiable
import po.misc.types.TypeRecord
import po.misc.types.castOrThrow
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1


sealed class RelationDelegate<DTO, DATA, ENTITY, F_DTO, FD, FE, V: Any>(
    val hostingDTO : CommonDTO<DTO, DATA, ENTITY>,
    val childModel: DTOClass<F_DTO, FD, FE>,
    val foreignEntityProperty: KMutableProperty1<FE, ENTITY>,
    val typeRecord : TypeRecord<V>,
): Component<DTO>(ComponentType.RelationDelegate, hostingDTO),  TasksManaged, Identifiable
        where DTO: ModelDTO, DATA: DataModel,  ENTITY : LongEntity,
              F_DTO: ModelDTO,  FD: DataModel, FE : LongEntity
{
    abstract val cardinality : Cardinality

    protected var onPropertyInitialized: ((KProperty<*>)-> Unit)? = null
    private var propertyParameter : KProperty<V>? = null
        set(value) {
            field = value
            onPropertyInitialized?.invoke(property)
        }
    val property: KProperty<V> get() = propertyParameter.getOrOperationsEx()
    val propertyName : String get() = propertyParameter?.name?:""
   // val pappedProperties : PropertyMapperRecord<V> = PropertyMapperRecord.createPropertyMap(typeRecord)


    protected val dtoType : TypeRecord<DTO>
        get() =  hostingDTO.dtoType

    protected val childType : TypeRecord<F_DTO>
        get() = childModel.config.registry.getRecord<F_DTO, InitException>(SourceObject.DTO)


    abstract fun getEffectiveValue():V
    protected abstract fun getChildDTOs(): List<CommonDTO<F_DTO, FD, FE>>
    abstract fun getData():List<FD>
    abstract fun getData(data:DATA):List<FD>
    abstract fun getEntities(entity:ENTITY):List<FE>
    protected abstract fun saveDto(dto:CommonDTO<F_DTO, FD, FE>, updateData: Boolean = false)
    protected abstract fun saveDto(dto: CommonDTO<F_DTO, FD, FE>, entity:FE)

    operator fun getValue(thisRef: DTO, property: KProperty<*>): V {
        return getEffectiveValue()
    }
    operator fun provideDelegate(thisRef: DTO, property: KProperty<*>): RelationDelegate<DTO, DATA, ENTITY, F_DTO, FD, FE, V> {
        propertyProvided(property)
        return this
    }
    private fun propertyProvided(property: KProperty<*>){
        if(propertyParameter == null){
            propertyParameter = property.castOrInitEx("Unable to cast KProperty<*> to KProperty<V>")
            hostingDTO.bindingHub.setRelationBinding(this.castOrInitEx())
        }
    }

    protected fun setParentDTO(childDTO: CommonDTO<F_DTO, FD, FE>){
        childDTO.setForeignDTO(hostingDTO)
    }

    fun createByData() {
        val hostingDtoEntity = hostingDTO.getEntity(this)
        val dataList = getData()
        dataList.forEach { data ->
            val newDto = childModel.newDTO(data)
            newDto.addTrackerInfo(CrudOperation.Insert, this)
            val insertedEntity = newDto.daoService.save {
                newDto.bindingHub.updateEntity(it)
                foreignEntityProperty.set(it, hostingDtoEntity)
            }
            saveDto(newDto, insertedEntity)
        }
        getChildDTOs().forEach { savedDto ->
            savedDto.bindingHub.createChildByData()
        }
    }

    fun updateFromData(data:DATA) {
        val childDtoList = getChildDTOs()
        getData(data).forEach { data ->
            val found = childDtoList.find { it.id == data.id }
            if (found != null) {
                found.bindingHub.updateFromData(data)
            } else {
                TODO("This branch not yet tested")
            }
        }
    }

    fun createByEntity(){
        getEntities(hostingDTO.getEntity(this@RelationDelegate)).forEach { entity->
           val newDto = childModel.createDTO(entity, CrudOperation.Select)
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
    typeRecord: TypeRecord<List<F_DTO>>
) : RelationDelegate<DTO, DATA, ENTITY, F_DTO, FD, FE, List<F_DTO>>(dto, childModel, foreignEntityProperty, typeRecord)
        where DTO : ModelDTO, DATA : DataModel, ENTITY : LongEntity,
              F_DTO: ModelDTO,  FD : DataModel, FE : LongEntity
{

    override val cardinality : Cardinality = Cardinality.ONE_TO_MANY

    private val dtos : MutableList<CommonDTO<F_DTO, FD, FE>> = mutableListOf()
    override fun getEffectiveValue(): List<F_DTO> {
        return dtos.map { it.castOrThrow<F_DTO, InitException>(childType.clazz) }
    }

    override fun getChildDTOs(): List<CommonDTO<F_DTO, FD, FE>> {
        return dtos.toList()
    }

    override fun getData(): List<FD> {
        return dataProperty.get(hostingDTO.dataModel)
    }
    override fun getData(data: DATA): List<FD> {
        return dataProperty.get(data)
    }
    override fun getEntities(entity: ENTITY): List<FE> {
        return entitiesProperty.get(entity).toList()
    }

    override fun saveDto(dto: CommonDTO<F_DTO, FD, FE>, entity:FE){
        setParentDTO(dto)
        dto.finalizeCreation(entity, cardinality)
        dtos.add(dto)
    }

    override fun saveDto(dto: CommonDTO<F_DTO, FD, FE>, updateData: Boolean) {
        setParentDTO(dto)
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
    typeRecord : TypeRecord<F_DTO>
) : RelationDelegate<DTO, DATA, ENTITY, F_DTO, FD, FE, F_DTO>(dto, childModel, foreignEntityProperty, typeRecord)
        where DTO : ModelDTO, DATA : DataModel, ENTITY : LongEntity,
              F_DTO: ModelDTO,  FD : DataModel, FE : LongEntity {

    override val cardinality : Cardinality = Cardinality.ONE_TO_ONE

    private var childDTO : CommonDTO<F_DTO, FD, FE>? = null
    override fun getEffectiveValue(): F_DTO {
        return childDTO.castOrThrow<F_DTO, InitException>(childType.clazz, "dto uninitialized in getEffectiveValue")
    }

    override fun getChildDTOs(): List<CommonDTO<F_DTO, FD, FE>> {
       return childDTO?.let { listOf(it) }?:emptyList()
    }
    override fun getData(): List<FD> {
        return listOf(dataProperty.get(hostingDTO.dataModel))
    }
    override fun getData(data: DATA): List<FD> {
        return listOf(dataProperty.get(data))
    }
    override fun getEntities(entity: ENTITY): List<FE> {
      return listOf(entityProperty.get(entity))
    }

    override fun saveDto(dto: CommonDTO<F_DTO, FD, FE>, entity:FE) {
        setParentDTO(dto)
        dto.finalizeCreation(entity, cardinality)
        childDTO = dto
    }

    override fun saveDto(dto: CommonDTO<F_DTO, FD, FE>, updateData: Boolean) {
        setParentDTO(dto)
        childDTO = dto
        if(updateData){
            dataProperty.set(hostingDTO.dataModel, dto.dataModel)
        }
    }

}
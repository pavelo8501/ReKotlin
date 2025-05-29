package po.exposify.dto.components.bindings.relation_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.SizedIterable
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOClass
import po.exposify.dto.components.bindings.helpers.createByEntity

import po.exposify.dto.enums.Cardinality
import po.exposify.dto.helpers.getTypeRecord
import po.exposify.dto.interfaces.ComponentType
import po.exposify.dto.interfaces.IdentifiableComponent
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.InitException
import po.exposify.exceptions.OperationsException
import po.exposify.extensions.castOrInitEx
import po.exposify.extensions.castOrOperationsEx
import po.exposify.extensions.getOrInitEx
import po.exposify.extensions.getOrOperationsEx
import po.lognotify.TasksManaged
import po.misc.registries.type.TypeRecord
import po.misc.types.castListOrThrow
import po.misc.types.castOrThrow
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1


sealed class RelationDelegate<DTO, DATA, ENTITY, F_DTO, FD, FE, V>(
    val hostingDTO : CommonDTO<DTO, DATA, ENTITY>, //Host
    val childModel: DTOClass<F_DTO, FD, FE>,  //Child Class
    val foreignEntityProperty: KMutableProperty1<FE, ENTITY>,
): TasksManaged, IdentifiableComponent
        where DTO: ModelDTO, DATA: DataModel,  ENTITY : LongEntity,
              F_DTO: ModelDTO,  FD: DataModel, FE : LongEntity
{

    abstract val componentName : String
    abstract val cardinality : Cardinality

    override val type: ComponentType = ComponentType.RelationBindingDelegate
    override val qualifiedName : String  get() = "$componentName[$]"

    protected var onPropertyInitialized: ((KProperty<*>)-> Unit)? = null
    private var propertyParameter : KProperty<*>? = null
        set(value) {
            field = value
            onPropertyInitialized?.invoke(property)
        }
    val property: KProperty<Any?> get() = propertyParameter.getOrOperationsEx()
    val propertyName : String get() = propertyParameter?.name?:""

    protected val dtoTypeRecord : TypeRecord<F_DTO> = childModel.getTypeRecord<F_DTO, FD, FE,  F_DTO>(ComponentType.DTO)
    protected val entityTypeRecord : TypeRecord<FE> = childModel.getTypeRecord<F_DTO, FD, FE, FE>(ComponentType.ENTITY)

    abstract fun getEffectiveValue():V
    abstract fun getData(data:DATA):List<FD>
    abstract fun getEntities(entity:ENTITY):List<FE>
    protected abstract fun saveDto(dto:CommonDTO<F_DTO, FD, FE>, updateData: Boolean = false)

    operator fun getValue(thisRef: DTO, property: KProperty<*>): V {
        return getEffectiveValue()
    }
    operator fun provideDelegate(thisRef: DTO, property: KProperty<*>): RelationDelegate<DTO, DATA, ENTITY, F_DTO, FD, FE, V> {
        propertyProvided(property)
        return this
    }
    private fun propertyProvided(property: KProperty<Any?>){
        if(propertyParameter == null){
            propertyParameter = property
            hostingDTO.bindingHub.setRelationBinding(this.castOrInitEx())
        }
    }

    fun createFromData(data: DATA){
        val dataList = getData(data)
        dataList.forEach {data->
          val dto =  childModel.config.dtoFactory.createDto(data)
            childModel.config.daoService.saveWithParent(dto){newEntity->
                foreignEntityProperty.set(newEntity, hostingDTO.entity)
            }
            saveDto(dto)
            dto.bindingHub.createFromData(dto)
        }
    }

    fun createFromEntity(entity: ENTITY){
         val entityList = getEntities(entity)
         entityList.createByEntity(childModel){
             saveDto(it, true)
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

    override val componentName: String =  "OneToManyDelegate"
    override val cardinality : Cardinality = Cardinality.ONE_TO_MANY

    private val dtos : MutableMap<Long, CommonDTO<F_DTO, FD, FE>> = mutableMapOf()
    override fun getEffectiveValue(): List<F_DTO> {
        return dtos.map { it.castOrThrow<F_DTO, InitException>(dtoTypeRecord.clazz) }
    }

    override fun getData(data: DATA): List<FD> {
        return dataProperty.get(data)
    }
    override fun getEntities(entity: ENTITY): List<FE> {
        return entitiesProperty.get(entity).toList()
    }

    override fun saveDto(dto: CommonDTO<F_DTO, FD, FE>, updateData: Boolean) {
        dtos[dto.id] = dto
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

    override val componentName: String =  "OneToManyDelegate"
    override val cardinality : Cardinality = Cardinality.ONE_TO_ONE


    private var dto : CommonDTO<F_DTO, FD, FE>? = null
    override fun getEffectiveValue(): F_DTO {
        return dto.castOrThrow<F_DTO, InitException>(dtoTypeRecord.clazz, "dto uninitialized in getEffectiveValue")
    }
    override fun getData(data: DATA): List<FD> {
        return listOf(dataProperty.get(data))
    }

    override fun getEntities(entity: ENTITY): List<FE> {
      return listOf(entityProperty.get(entity))
    }

    override fun saveDto(dto: CommonDTO<F_DTO, FD, FE>, updateData: Boolean) {
        this.dto = dto
        if(updateData){
            dataProperty.set(hostingDTO.dataModel, dto.dataModel)
        }
    }



}
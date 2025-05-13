package po.exposify.dto.components.relation_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.SizedIterable
import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.MultipleRepository
import po.exposify.dto.components.SingleRepository
import po.exposify.dto.components.proFErty_binder.EntityUpdateContainer
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.OperationsException
import po.lognotify.TasksManaged
import po.misc.collections.generateKey
import po.misc.types.castListOrThrow
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

class OneToOneDelegate<DTO, DATA, ENTITY, C_DTO,  CD,  FE>(
    private val dto : CommonDTO<DTO, DATA, ENTITY>,
    private val childModel:DTOBase<C_DTO, CD, FE>,
    private val dataProperty: KMutableProperty1<DATA, CD>,
    private val entityProperty: KProperty1<ENTITY, FE>,
    private val foreignEntity: KMutableProperty1<FE, ENTITY>,
) : RelationBindingDelegate<DTO, DATA, ENTITY, C_DTO,  CD, FE, CommonDTO<C_DTO, CD, FE>>()
        where DTO : ModelDTO, DATA : DataModel, ENTITY : LongEntity,
              C_DTO: ModelDTO,  CD : DataModel, FE : LongEntity
{
    override val qualifiedName : String  get() = "OneToOneDelegate[${dto.dtoName}::${dataProperty.name}]"

    val singleRepository : SingleRepository<DTO, DATA, ENTITY, C_DTO, CD, FE> by lazy {
        dto.getRepository<C_DTO, CD, FE>(dto.dtoClass.generateKey(Cardinality.ONE_TO_ONE)) as SingleRepository
    }

    override fun getEffectiveValue():CommonDTO<C_DTO, CD, FE>{
        return singleRepository.getDTO()
    }

    fun getDataModel(dataModel: DATA): CD{
       return dataProperty.get(dataModel)
    }
    fun saveDataModel(dataModel:CD){
        dataProperty.set(dto.dataModel, dataModel)
    }

    fun attachForeignEntity(container: EntityUpdateContainer<FE, DTO, DATA, ENTITY>){
        foreignEntity.set(container.ownEntity, dto.daoEntity)
    }

    fun getForeignEntity(entity: ENTITY): FE{
        return entityProperty.get(entity)
    }

    override fun getForeignEntity(id: Long): FE?{
       val childEntity = entityProperty.get(dto.daoEntity)
       return if(childEntity.id.value == id){
             childEntity
        }else{
            null
       }
    }
}

class OneToManyDelegate<DTO, DATA, ENTITY, F_DTO, FD, FE>(
    private val dto : CommonDTO<DTO, DATA, ENTITY>,
    private val childModel: DTOBase<F_DTO, FD, FE>,
    private val dataProperty: KProperty1<DATA, MutableList<FD>>,
    private val entitiesProperty: KProperty1<ENTITY, SizedIterable<FE>>,
    private val foreignEntity: KMutableProperty1<FE, ENTITY>,
) : RelationBindingDelegate<DTO, DATA, ENTITY, F_DTO, FD, FE, List<F_DTO>>()
        where DTO : ModelDTO, DATA : DataModel, ENTITY : LongEntity,
              F_DTO: ModelDTO,  FD : DataModel, FE : LongEntity {

    override val qualifiedName : String  get() = "OneToManyDelegate[${dto.dtoName}::${dataProperty.name}]"

    val multipleRepository : MultipleRepository<DTO, DATA, ENTITY, F_DTO, FD, FE> by lazy {
        dto.getRepository<F_DTO, FD, FE>(dto.dtoClass.generateKey(Cardinality.ONE_TO_MANY)) as MultipleRepository
    }

    override fun getEffectiveValue(): List<F_DTO> {
        return multipleRepository.getDTO().castListOrThrow<F_DTO, OperationsException>(childModel.config.registryRecord.derivedDTOClazz, null, 0)
    }

    fun saveDataModels(foreignDataModels: List<FD>){
        dataProperty.get(dto.dataModel).addAll(foreignDataModels)
    }

    fun getDataModels(dataModel: DATA): List<FD>{
        return dataProperty.get(dataModel).toList()
    }

    fun getForeignEntities(entity: ENTITY): List<FE>{
       return entitiesProperty.get(entity).toList()
    }

    override fun getForeignEntity(id: Long): FE?{
       return entitiesProperty.get(dto.daoEntity).firstOrNull { it.id.value == id }
    }

   suspend fun processForeignEntities(entity: ENTITY, processFn:suspend (List<FE>)-> List<CommonDTO<F_DTO, FD, FE>>){
        val foreignEntities = entitiesProperty.get(entity).toList()
        val foreignDtos =  processFn.invoke(foreignEntities)
        val mutableListOfForeignDataModels =   dataProperty.get(dto.dataModel)
        foreignDtos.forEach {
            mutableListOfForeignDataModels.add(it.dataModel)
        }
    }

    fun attachForeignEntity(container: EntityUpdateContainer<FE, DTO, DATA, ENTITY>){
        foreignEntity.set(container.ownEntity, dto.daoEntity)
    }
}

sealed class RelationBindingDelegate<DTO, DATA, ENTITY, C_DTO, FD, FE, R>(

): ReadOnlyProperty<DTO, R>, TasksManaged
        where DTO: ModelDTO, DATA: DataModel,  ENTITY : LongEntity,
              C_DTO: ModelDTO,  FD: DataModel, FE : LongEntity{

    abstract val qualifiedName : String

    abstract fun getForeignEntity(id: Long):FE?


    abstract fun getEffectiveValue():R
    override fun getValue(thisRef: DTO, property: KProperty<*>): R{
        return getEffectiveValue()
    }
}
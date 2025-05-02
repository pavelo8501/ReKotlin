package po.exposify.dto.components.relation_binder.delegates

import org.jetbrains.exposed.sql.SizedIterable
import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.MultipleRepository
import po.exposify.dto.components.SingleRepository
import po.exposify.dto.components.proFErty_binder.EntityUpdateContainer
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntity
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.lognotify.TasksManaged
import po.misc.collections.generateKey
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

class OneToOneDelegate<DTO, DATA, ENTITY, C_DTO,  CD,  FE>(
    private val dto : CommonDTO<DTO, DATA, ENTITY>,
    private val childModel:DTOBase<C_DTO, CD>,
    private val dataProperty: KMutableProperty1<DATA, CD>,
    private val ownEntities: KProperty1<ENTITY, FE>,
    private val foreignEntity: KMutableProperty1<FE, ENTITY>,
) : RelationBindingDelegates<DTO, ENTITY, C_DTO,  CD, FE, CommonDTO<C_DTO, CD, FE>>()
        where DTO : ModelDTO, DATA : DataModel, ENTITY : ExposifyEntity,
              C_DTO: ModelDTO,  CD : DataModel, FE : ExposifyEntity
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

    fun getChildEntity(entity: ENTITY): FE{
        return ownEntities.get(entity)
    }
}

class OneToManyDelegate<DTO, DATA, ENTITY, C_DTO, FD, FE>(
    private val dto : CommonDTO<DTO, DATA, ENTITY>,
    private val childModel: DTOBase<C_DTO, FD>,
    private val dataProperty: KProperty1<DATA, MutableList<FD>>,
    private val foreignEntities: KProperty1<ENTITY, SizedIterable<FE>>,
    private val foreignEntity: KMutableProperty1<FE, ENTITY>,
) : RelationBindingDelegates<DTO, ENTITY, C_DTO, FD, FE, List<CommonDTO<C_DTO, FD, FE>>>()
        where DTO : ModelDTO, DATA : DataModel, ENTITY : ExposifyEntity,
              C_DTO: ModelDTO,  FD : DataModel, FE : ExposifyEntity {

    override val qualifiedName : String  get() = "OneToManyDelegate[${dto.dtoName}::${dataProperty.name}]"

    val multipleRepository : MultipleRepository<DTO, DATA, ENTITY, C_DTO, FD, FE> by lazy {
        dto.getRepository<C_DTO, FD, FE>(dto.dtoClass.generateKey(Cardinality.ONE_TO_MANY)) as MultipleRepository
    }

    override fun getEffectiveValue(): List<CommonDTO<C_DTO, FD, FE>> {
        return multipleRepository.getDTO()
    }

    fun saveDataModels(foreignDataModels: List<FD>){
        dataProperty.get(dto.dataModel).addAll(foreignDataModels)
    }

    fun getDataModels(dataModel: DATA): List<FD>{
        return dataProperty.get(dataModel).toList()
    }

    fun getForeignEntities(entity: ENTITY): List<FE>{
       return foreignEntities.get(entity).toList()
    }

   suspend fun processForeignEntities(entity: ENTITY, processFn:suspend (List<FE>)-> List<CommonDTO<C_DTO, FD, FE>>){
        val foreignEntities = foreignEntities.get(entity).toList()
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

sealed class RelationBindingDelegates<DTO,ENTITY,C_DTO, CD,  CE,  R>(

): ReadOnlyProperty<DTO, R>, TasksManaged
        where DTO: ModelDTO, ENTITY : ExposifyEntity,C_DTO: ModelDTO,  CD: DataModel, CE : ExposifyEntity{

    abstract val qualifiedName : String
    abstract fun getEffectiveValue():R
    override fun getValue(thisRef: DTO, property: KProperty<*>): R{
        return getEffectiveValue()
    }
}
package po.exposify.dto.components.relation_binder.delegates

import org.jetbrains.exposed.sql.SizedIterable
import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.MultipleRepository
import po.exposify.dto.components.SingleRepository
import po.exposify.dto.components.property_binder.EntityUpdateContainer
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.components.relation_binder.BindingKeyBase
import po.exposify.dto.components.relation_binder.MultipleChildContainer
import po.exposify.dto.components.relation_binder.SingleChildContainer
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntity
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.castOrOperationsEx
import po.lognotify.TasksManaged
import po.lognotify.extensions.subTask
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

class OneToOneDelegate<DTO, DATA, ENTITY, C_DTO,  CD,  CE>(
    private val dto : CommonDTO<DTO, DATA, ENTITY>,
    private val childModel:DTOBase<C_DTO, CD>,
    private val ownDataModel: KProperty1<DATA, CD>,
    private val ownEntities: KProperty1<ENTITY, CE>,
    private val foreignEntity: KMutableProperty1<CE, ENTITY>,
) : RelationBindingDelegates<DTO, ENTITY, C_DTO,  CD, CE, CommonDTO<C_DTO, CD, CE>>()
        where DTO : ModelDTO, DATA : DataModel, ENTITY : ExposifyEntity,
              C_DTO: ModelDTO,  CD : DataModel, CE : ExposifyEntity
{
    override val qualifiedName : String  get() = "OneToOneDelegate[${ownDataModel.name}]"

    val singleRepository : SingleRepository<DTO, DATA, ENTITY, C_DTO, CD, CE> by lazy {
        dto.getRepository<C_DTO, CD, CE>(BindingKeyBase.createOneToOneKey(childModel)) as SingleRepository
    }

    override fun getEffectiveValue():CommonDTO<C_DTO, CD, CE>{
        return singleRepository.getDTO()
    }

    fun getDataModel(dataModel: DATA): CD{
       return ownDataModel.get(dataModel)
    }

    fun setForeignEntity(container: EntityUpdateContainer<ENTITY, C_DTO, CD, CE>){
        container.parentDto?.let {
            foreignEntity.set(it.daoEntity, container.ownEntity)
        }?:run {
            throw OperationsException("setForeignEntity,  container.parentDto is null", ExceptionCode.VALUE_NOT_FOUND)
        }
    }

    fun getChildEntity(entity: ENTITY): CE{
        return ownEntities.get(entity)
    }
}

class OneToManyDelegate<DTO, DATA, ENTITY, C_DTO, CD, CE>(
    private val dto : CommonDTO<DTO, DATA, ENTITY>,
    private val childModel: DTOBase<C_DTO, CD>,
    private val ownDataModels: KProperty1<DATA, MutableList<CD>>,
    private val ownEntities: KProperty1<ENTITY, SizedIterable<CE>>,
    private val foreignEntity: KMutableProperty1<CE, ENTITY>,
) : RelationBindingDelegates<DTO, ENTITY, C_DTO, CD, CE, List<CommonDTO<C_DTO, CD, CE>>>()
        where DTO : ModelDTO, DATA : DataModel, ENTITY : ExposifyEntity,
              C_DTO: ModelDTO,  CD : DataModel, CE : ExposifyEntity {

    override val qualifiedName : String  get() = "OneToManyDelegate[${ownDataModels.name}]"

    val multipleRepository : MultipleRepository<DTO, DATA, ENTITY, C_DTO, CD, CE> by lazy {
        dto.getRepository<C_DTO, CD, CE>(BindingKeyBase.createOneToOneKey(childModel)) as MultipleRepository
    }

    override fun getEffectiveValue(): List<CommonDTO<C_DTO, CD, CE>> {
        return multipleRepository.getDTO()
    }

    fun getDataModels(dataModel: DATA): List<CD>{
        return ownDataModels.get(dataModel).toList()
    }

    fun getChildEntities(entity: ENTITY): List<CE>{
        return ownEntities.get(entity).toList()
    }

    fun setForeignEntity(container: EntityUpdateContainer<ENTITY, C_DTO, CD, CE>){
        container.parentDto?.let {
            foreignEntity.set(it.daoEntity, container.ownEntity)
        }?:run { 
            throw OperationsException("setForeignEntity,  container.parentDto is null", ExceptionCode.VALUE_NOT_FOUND)
        }
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
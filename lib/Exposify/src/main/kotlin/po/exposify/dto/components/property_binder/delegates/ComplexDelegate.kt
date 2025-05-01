package po.exposify.dto.components.property_binder.delegates

import org.jetbrains.exposed.dao.LongEntityClass
import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.property_binder.EntityUpdateContainer
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntity
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.castOrOperationsEx
import po.exposify.extensions.getOrOperationsEx
import po.lognotify.TasksManaged
import po.lognotify.extensions.subTask
import po.misc.types.castOrThrow
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

fun <T, R> KMutableProperty1<T, R>.name(): String = this.name

class ForeignIDClassDelegate<DTO, DATA, ENTITY, FOREIGN_ENTITY>(
    dto: CommonDTO<DTO, DATA, ENTITY>,
    dataProperty : KMutableProperty1<DATA, Long>,
    val entityProperty : KMutableProperty1<ENTITY, FOREIGN_ENTITY>,
    val foreignEntityModel: LongEntityClass<FOREIGN_ENTITY>,
): ComplexDelegate<DTO, DATA, ENTITY, FOREIGN_ENTITY, Long, Long>(dto, dataProperty, entityProperty.name)
    where DATA: DataModel, ENTITY: ExposifyEntity, DTO: ModelDTO, FOREIGN_ENTITY: ExposifyEntity
{
    override fun updateByEntity(
        updateMode: UpdateMode,
        container: EntityUpdateContainer<ENTITY, *, *, FOREIGN_ENTITY>
    ) {
        val value = getEffectiveValue()
        val foreignEntity = foreignEntityModel.get(value)
        println("foreignEntity ${foreignEntity::class.simpleName}")
        val ownEntity = container.ownEntity
        println("ownEntity ${ownEntity::class.simpleName}")

        entityProperty.set(ownEntity, foreignEntity)
    }

    override fun updateDataModel(entity: ENTITY){
       val foreignEntity = entityProperty.get(entity)
       val id = foreignEntity.id.value
       dataProperty.set(dto.dataModel, id)
    }
    override fun getEffectiveValue(): Long{
        return dataProperty.get(dto.dataModel)
    }
}

class ParentIDDelegate<DTO, DATA, ENTITY, FOREIGN_ENTITY>(
    dto: CommonDTO<DTO, DATA, ENTITY>,
    dataProperty : KMutableProperty1<DATA, Long>,
    val entityProperty : KMutableProperty1<ENTITY, FOREIGN_ENTITY>,
): ComplexDelegate<DTO, DATA, ENTITY, FOREIGN_ENTITY, Long, Long>(dto, dataProperty, entityProperty.name)
        where DATA: DataModel, ENTITY: ExposifyEntity, DTO : ModelDTO, FOREIGN_ENTITY: ExposifyEntity
{

    override fun updateByEntity(
        updateMode: UpdateMode,
        container: EntityUpdateContainer<ENTITY, *, *, FOREIGN_ENTITY>
    ){
        if(container.parentDataSet){
            val parentEntity = container.hasParentEntity
            println("parentEntity ${parentEntity::class.simpleName}")
            val ownEntity = container.ownEntity
            println("ownEntity ${ownEntity::class.simpleName}")
            entityProperty.set(ownEntity, parentEntity)
        }
    }

    override fun updateDataModel(entity: ENTITY){
        val foreignEntity =  entityProperty.get(entity)
        val id = foreignEntity.id.value
        dataProperty.set(dto.dataModel, id)
    }
    override fun getEffectiveValue(): Long{
        return dataProperty.get(dto.dataModel)
    }
}

class ParentDelegate<DTO, DATA, ENTITY, PARENT_DTO, PARENT_DATA, PARENT_ENTITY>(
    dto: CommonDTO<DTO, DATA, ENTITY>,
    dataProperty : KMutableProperty1<DATA, PARENT_DATA>,
    private val parentDtoModel: DTOClass<PARENT_DTO>,
    private val entityModel: LongEntityClass<PARENT_ENTITY>
): ComplexDelegate<DTO, DATA, ENTITY, PARENT_ENTITY, PARENT_DATA, CommonDTO<PARENT_DTO, PARENT_DATA, PARENT_ENTITY>>
    (dto, dataProperty, dataProperty.name)
        where DATA: DataModel, ENTITY: ExposifyEntity, DTO : ModelDTO, PARENT_DTO: ModelDTO,
              PARENT_DATA : DataModel, PARENT_ENTITY: ExposifyEntity
{

    var parentDto : CommonDTO<PARENT_DTO, PARENT_DATA, PARENT_ENTITY>? = null

    override fun updateByEntity(
        updateMode: UpdateMode,
        container: EntityUpdateContainer<ENTITY, *, *, PARENT_ENTITY>
    ) {
        if(container.parentDataSet){
            val foreignDto = container.hasParentDto.castOrThrow<CommonDTO<PARENT_DTO, PARENT_DATA, PARENT_ENTITY>, OperationsException>()
            dataProperty.set(dto.dataModel, foreignDto.dataModel)
            parentDto = foreignDto
        }
    }

    override fun updateDataModel(entity: ENTITY){
       val foreignDto = parentDtoModel.lookupDTO<DTO>(entity.id.value, dto.dtoClass)
        foreignDto?.let {
            val castedParentDto = it.castOrThrow<CommonDTO<PARENT_DTO, PARENT_DATA, PARENT_ENTITY>, OperationsException>()
            dataProperty.set(dto.dataModel, castedParentDto.dataModel)
        }
    }

    override fun getEffectiveValue():  CommonDTO<PARENT_DTO, PARENT_DATA, PARENT_ENTITY>{
        return parentDto.getOrOperationsEx("Parent dto should have been initialized", ExceptionCode.ABNORMAL_STATE)
    }

}

sealed class ComplexDelegate<DTO, DATA, ENTITY, PARENT_ENTITY, DATA_VAL, RES_VAL>(
    protected val dto: CommonDTO<DTO, DATA, ENTITY>,
    protected val dataProperty : KMutableProperty1<DATA, DATA_VAL>,
    protected val entityPropertyName : String
): ReadOnlyProperty<DTO, RES_VAL>, TasksManaged
    where DATA: DataModel, ENTITY: ExposifyEntity, DTO : ModelDTO, PARENT_ENTITY: ExposifyEntity
{


    var delegateProperty: KProperty<*>? = null
    var delegatePropertyName: String = ""

    override fun getValue(thisRef: DTO, property: KProperty<*>):RES_VAL{
        delegatePropertyName = property.name
        delegateProperty = property
        return getEffectiveValue()
    }

    abstract fun getEffectiveValue():RES_VAL
    abstract fun updateByEntity(
        updateMode: UpdateMode,
        container:  EntityUpdateContainer<ENTITY, *, *, PARENT_ENTITY>)
    abstract fun updateDataModel(entity: ENTITY)

    suspend fun entityBeforeInsertedUpdate(
        updateMode: UpdateMode,
        updateContainer: EntityUpdateContainer<ENTITY, *, *, ExposifyEntity>
    ): Unit
        = subTask("BeforeInsertedUpdate"){handler->
        if(updateMode == UpdateMode.MODEL_TO_ENTITY || updateMode == UpdateMode.MODEL_TO_ENTITY_FORCED){
            val castedContainer = updateContainer.castOrOperationsEx<EntityUpdateContainer<ENTITY, *, *, PARENT_ENTITY>>()
            updateByEntity(updateMode, castedContainer)
        }else{
            updateDataModel(updateContainer.ownEntity)
        }
    }.resultOrException()

    suspend fun <P_DTO : ModelDTO, PD : DataModel, PE : ExposifyEntity> entityAfterInsertedUpdate(
        updateMode: UpdateMode,
        updateContainer: EntityUpdateContainer<ENTITY, P_DTO, PD, PE>
    ): Unit
        = subTask("AfterInserted"){handler->
      //  updateDataModel(updateContainer.ownEntity)
    }.resultOrException()

}

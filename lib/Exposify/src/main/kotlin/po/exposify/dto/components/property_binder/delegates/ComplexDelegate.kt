package po.exposify.dto.components.property_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.exposify.dto.DTOClass
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.proFErty_binder.EntityUpdateContainer
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.castOrOperationsEx
import po.exposify.extensions.getOrOperationsEx
import po.exposify.extensions.withTransactionIfNone
import po.lognotify.TasksManaged
import po.lognotify.extensions.subTask
import po.misc.types.castOrThrow
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

fun <T, R> KMutableProperty1<T, R>.name(): String = this.name

class ForeignIDClassDelegate<DTO, DATA, ENTITY, FE>(
    dto: CommonDTO<DTO, DATA, ENTITY>,
    dataProperty : KMutableProperty1<DATA, Long>,
    val entityProperty : KMutableProperty1<ENTITY, FE>,
    val foreignEntityModel: LongEntityClass<FE>,
): ComplexDelegate<DTO, DATA, ENTITY, FE, Long, Long>(dto, dataProperty, entityProperty.name)
    where DATA: DataModel, ENTITY: LongEntity, DTO: ModelDTO, FE: LongEntity
{
    override val  qualifiedName : String = "ForeignIDClassDelegate[${dto.dtoName}::${dataProperty.name}]"

    override fun update(isBeforeInserted:  Boolean, container:  EntityUpdateContainer<ENTITY, *, *, FE>){
        //MODEL_TO_ENTITY effectively on Update/Save
        if(container.updateMode == UpdateMode.MODEL_TO_ENTITY || container.updateMode == UpdateMode.MODEL_TO_ENTITY_FORCED){
            val value = getEffectiveValue()
            val foreignEntity = foreignEntityModel[value]
            val ownEntity = container.ownEntity
            entityProperty.set(ownEntity, foreignEntity)
        }else{
            val id = entityProperty.get(container.ownEntity).id.value
            dataProperty.set(dto.dataModel, id)
        }
    }
    override fun getEffectiveValue(): Long{
        return dataProperty.get(dto.dataModel)
    }
}

class ParentIDDelegate<DTO, DATA, ENTITY, FE>(
    dto: CommonDTO<DTO, DATA, ENTITY>,
    dataProperty : KMutableProperty1<DATA, Long>,
    val entityProperty : KMutableProperty1<ENTITY, FE>,
): ComplexDelegate<DTO, DATA, ENTITY, FE, Long, Long>(dto, dataProperty, entityProperty.name)
        where DATA: DataModel, ENTITY: LongEntity, DTO : ModelDTO, FE: LongEntity
{
    override val qualifiedName : String = "ParentIDDelegate[${dto.dtoName}::${dataProperty.name}]"

    override fun update(isBeforeInserted: Boolean, container: EntityUpdateContainer<ENTITY, *, *, FE>){
        if(container.updateMode == UpdateMode.MODEL_TO_ENTITY || container.updateMode == UpdateMode.MODEL_TO_ENTITY_FORCED){
            container.parentDto?.let {
                val foreignEntity = it.daoEntity
                entityProperty.set(container.ownEntity, foreignEntity)
                dataProperty.set(dto.dataModel, foreignEntity.id.value)
            }
        }else{
            dataProperty.set(dto.dataModel, container.ownEntity.id.value)
        }
    }

    override fun getEffectiveValue(): Long{
        return dataProperty.get(dto.dataModel)
    }
}

class ParentDelegate<DTO, DATA, ENTITY, F_DTO, FD, FE>(
    dto: CommonDTO<DTO, DATA, ENTITY>,
    dataProperty : KMutableProperty1<DATA, FD>,
    private val parentDtoModel: DTOClass<F_DTO, FD, FE>,
    private val entityModel: LongEntityClass<FE>
): ComplexDelegate<DTO, DATA, ENTITY, FE, FD, CommonDTO<F_DTO, FD, FE>>(dto, dataProperty, dataProperty.name)
        where DATA: DataModel, ENTITY: LongEntity, DTO : ModelDTO, F_DTO: ModelDTO, FD : DataModel, FE: LongEntity
{
    override val  qualifiedName : String = "ParentDelegate[${dto.dtoName}::${dataProperty.name}]"
    var parentDto : CommonDTO<F_DTO, FD, FE>? = null

    override fun update(
        isBeforeInserted:  Boolean,
        container:  EntityUpdateContainer<ENTITY, *, *, FE>
    ){
        if(container.updateMode == UpdateMode.ENTITY_TO_MODEL || container.updateMode == UpdateMode.ENTITY_TO_MODEL_FORCED){
            if(container.isParentDtoSet){
                val foreignDto = container.hasParentDto.castOrThrow<CommonDTO<F_DTO, FD, FE>, OperationsException>()
                dataProperty.set(dto.dataModel, foreignDto.dataModel)
                parentDto = foreignDto
            }
        }else{
            val foreignDto = parentDtoModel.lookupDTO(dto.daoEntity.id.value, dto.dtoClass)
            foreignDto?.let {
                val castedParentDto = it.castOrThrow<CommonDTO<F_DTO, FD, FE>, OperationsException>()
                dataProperty.set(dto.dataModel, castedParentDto.dataModel)
            }
        }
    }
    
    override fun getEffectiveValue():  CommonDTO<F_DTO, FD, FE>{
        return parentDto.getOrOperationsEx("Parent dto should have been initialized", ExceptionCode.ABNORMAL_STATE)
    }
}

sealed class ComplexDelegate<DTO, DATA, ENTITY, FE, DATA_VAL, RES_VAL>(
    protected val dto: CommonDTO<DTO, DATA, ENTITY>,
    protected val dataProperty : KMutableProperty1<DATA, DATA_VAL>,
    protected val entityPropertyName : String
): ReadOnlyProperty<DTO, RES_VAL>, TasksManaged
    where DATA: DataModel, ENTITY: LongEntity, DTO : ModelDTO, FE: LongEntity
{
   private var delegateProperty: KProperty<*>? = null
   private var delegatePropertyName: String = ""

   abstract val qualifiedName : String

   override fun getValue(thisRef: DTO, property: KProperty<*>):RES_VAL{
        delegatePropertyName = property.name
        delegateProperty = property
        return getEffectiveValue()
   }

    abstract fun getEffectiveValue():RES_VAL
    protected abstract fun update(isBeforeInserted: Boolean, container: EntityUpdateContainer<ENTITY, *, *, FE>)
    suspend fun <F_DTO: ModelDTO, FD: DataModel, FFE: LongEntity>  beforeInsertedUpdate(
        updateContainer: EntityUpdateContainer<ENTITY, F_DTO, FD, FFE>
    ): Unit = subTask("BeforeInsertedUpdate", qualifiedName){

        val castedContainer = updateContainer.castOrOperationsEx<EntityUpdateContainer<ENTITY, F_DTO, FD, FE>>()
        update(true, castedContainer)

    }.resultOrException()

    suspend fun <F_DTO: ModelDTO, FD: DataModel, FFE: LongEntity> afterInsertedUpdate(
        updateContainer: EntityUpdateContainer<ENTITY, F_DTO, FD, FFE>
    ): Unit = subTask("AfterInsertedUpdate", qualifiedName){handler->
        val castedContainer = updateContainer.castOrOperationsEx<EntityUpdateContainer<ENTITY, F_DTO, FD, FE>>()
        withTransactionIfNone(handler) {
            update(false, castedContainer)
        }
    }.resultOrException()

}

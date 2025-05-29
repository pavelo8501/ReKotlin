package po.exposify.dto.components.bindings.property_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOClass
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.components.proFErty_binder.EntityUpdateContainer
import po.exposify.dto.components.bindings.property_binder.enums.UpdateMode
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

class ForeignIDClassDelegate<DTO, D, E, F_DTO, FD, FE>(
    dto: CommonDTO<DTO, D, E>,
    dataProperty : KMutableProperty1<D, Long>,
    val entityProperty : KMutableProperty1<E, FE>,
    val foreignDTOClass:  DTOBase<F_DTO, FD, FE>,
): ComplexDelegate<DTO, D, E, F_DTO, FD, FE, Long, Long>(dto, dataProperty, entityProperty.name)
    where D: DataModel, E: LongEntity, DTO: ModelDTO,
          F_DTO: ModelDTO, FD: DataModel, FE: LongEntity
{
    override val  qualifiedName : String = "ForeignIDClassDelegate[${dto.dtoName}::${dataProperty.name}]"

    override fun update(isBeforeInserted:  Boolean, container:  EntityUpdateContainer<E, F_DTO, FD, FE>){
        //MODEL_TO_ENTITY effectively on Update/Save
        if(container.updateMode == UpdateMode.MODEL_TO_ENTITY){
            val value = getEffectiveValue()
            val foreignEntity = foreignDTOClass.config.entityModel[value]
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

class ParentIDDelegate<DTO, D, E, FE>(
    dto: CommonDTO<DTO, D, E>,
    dataProperty : KMutableProperty1<D, Long>,
    val entityProperty : KMutableProperty1<E, FE>,
): ComplexDelegate<DTO, D, E, ModelDTO, DataModel, FE, Long, Long>(dto, dataProperty, entityProperty.name)
        where DTO : ModelDTO, D: DataModel, E: LongEntity, FE: LongEntity
{
    override val qualifiedName : String = "ParentIDDelegate[${dto.dtoName}::${dataProperty.name}]"

    override fun update(isBeforeInserted: Boolean, container: EntityUpdateContainer<E, ModelDTO, DataModel, FE>){

        if(container.updateMode == UpdateMode.MODEL_TO_ENTITY){
            container.parentDto?.let {
                val foreignEntity = it.entity
                dataProperty.set(dto.dataModel, foreignEntity.id.value)
            }
        }else{
            val foreignId =  entityProperty.get(container.ownEntity).id.value
            dataProperty.set(dto.dataModel, foreignId)
        }
    }

    override fun getEffectiveValue(): Long{
        return dataProperty.get(dto.dataModel)
    }
}

class ParentDelegate<DTO, D, ENTITY, F_DTO, FD, FE>(
    dto: CommonDTO<DTO, D, ENTITY>,
    dataProperty : KMutableProperty1<D, FD>,
    private val parentDtoModel: DTOClass<F_DTO, FD, FE>,
    private val foreignDTOClass:  DTOClass<F_DTO, FD, FE>,
): ComplexDelegate<DTO, D, ENTITY,F_DTO, FD, FE, FD, CommonDTO<F_DTO, FD, FE>>(dto, dataProperty, dataProperty.name)
        where D: DataModel, ENTITY: LongEntity, DTO : ModelDTO, F_DTO: ModelDTO, FD : DataModel, FE: LongEntity
{
    override val  qualifiedName : String = "ParentDelegate[${dto.dtoName}::${dataProperty.name}]"
    var parentDto : CommonDTO<F_DTO, FD, FE>? = null

    override fun update(
        isBeforeInserted:  Boolean,
        container:  EntityUpdateContainer<ENTITY, F_DTO, FD, FE>
    ){
        if(container.updateMode == UpdateMode.MODEL_TO_ENTITY){
            val foreignDto = parentDtoModel.lookupDTO(dto.entity.id.value)
            foreignDto?.let {
                val castedParentDto = it.castOrThrow<CommonDTO<F_DTO, FD, FE>, OperationsException>()
                dataProperty.set(dto.dataModel, castedParentDto.dataModel)
            }
        }else{
            if(container.isParentDtoSet){
                val foreignDto = container.hasParentDto.castOrThrow<CommonDTO<F_DTO, FD, FE>, OperationsException>()
                dataProperty.set(dto.dataModel, foreignDto.dataModel)
                parentDto = foreignDto
            }
        }
    }
    
    override fun getEffectiveValue():  CommonDTO<F_DTO, FD, FE>{
        return parentDto.getOrOperationsEx("Parent dto should have been initialized", ExceptionCode.ABNORMAL_STATE)
    }
}

sealed class ComplexDelegate<DTO, D, E, F_DTO, FD,  FE, DATA_VAL, RES_VAL>(
    protected val dto: CommonDTO<DTO, D, E>,
    protected val dataProperty : KMutableProperty1<D, DATA_VAL>,
    protected val entityPropertyName : String
): ReadOnlyProperty<DTO, RES_VAL>, TasksManaged
    where D: DataModel, E: LongEntity, DTO : ModelDTO,
          F_DTO: ModelDTO, FD : DataModel,  FE: LongEntity
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

    protected abstract fun update(isBeforeInserted: Boolean, container: EntityUpdateContainer<E, F_DTO, FD, FE>)

    fun  beforeInsertedUpdate(
        updateContainer: EntityUpdateContainer<E, *, *, *>
    ): Unit = subTask("BeforeInsertedUpdate"){
        update(true, updateContainer.castOrOperationsEx())

    }.resultOrException()

    fun  afterInsertedUpdate(
        updateContainer: EntityUpdateContainer<E, *, *, *>
    ): Unit = subTask("AfterInsertedUpdate"){handler->
        withTransactionIfNone(handler) {
            update(false, updateContainer.castOrOperationsEx())
        }
    }.resultOrException()

}

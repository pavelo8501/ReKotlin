package po.db.data_service.models

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.binder.PropertyBinder
import po.db.data_service.binder.UpdateMode
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DTOEntity
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.OperationsException

abstract class EntityDTO<DATA, ENTITY>(
    injectedDataModel : DATA
): DTOContainerBase<DATA, ENTITY>(injectedDataModel), DTOEntity<DATA, ENTITY>, Cloneable
        where DATA: DataModel , ENTITY : LongEntity

abstract class CommonDTO<DATA>(
    injectedDataModel : DATA
): DTOContainerBase<DATA, LongEntity>(injectedDataModel), DTOEntity<DATA, LongEntity>, Cloneable
        where DATA: DataModel {

    val childDTOs = mutableListOf<CommonDTO<DATA>>()

    public override fun clone(): DataModel = this.clone()

    fun <ENTITY: LongEntity>copyAsEntityDTO(dtoClass: DTOClass<DATA, ENTITY>): EntityDTO<DATA, ENTITY> {
        return  copyAsEntityDTO(injectedDataModel, dtoClass)
    }
}

sealed class DTOContainerBase<DATA, ENTITY>(
    val injectedDataModel : DATA
) where DATA : DataModel, ENTITY: LongEntity{

    var onInitializationStatusChange : ((DTOContainerBase<DATA, ENTITY>)-> Unit)? = null
    var initStatus: DTOInitStatus = DTOInitStatus.UNINITIALIZED
        set(value){
            if(value!= field){
                field = value
                onInitializationStatusChange?.invoke(this)
            }
        }

    var id : Long
        get(){return injectedDataModel.id}
        set(value) {injectedDataModel.id = value}

   private  var _sourceModel: DTOClass<DATA, ENTITY>? = null
   var sourceModel : DTOClass<DATA, ENTITY>
        get(){return  _sourceModel?: throw OperationsException(
            "Trying to access dtoModel property of CommonDTOV2 id :$id while undefined",
            ExceptionCodes.LAZY_NOT_INITIALIZED) }
        set(value){ _sourceModel = value}

   private var _entityDAO : ENTITY? = null
   var entityDAO : ENTITY
        set(value){  _entityDAO = value }
        get(){return  _entityDAO?:throw OperationsException(
            "Entity uninitialized",
            ExceptionCodes.LAZY_NOT_INITIALIZED) }

    val isUnsaved : Boolean
        get(){
            return id == 0L
        }

   val propertyBinder: PropertyBinder<DATA,ENTITY> by lazy { initialize(sourceModel) }

   fun toDataModel(): DATA =  this.injectedDataModel

   fun initialize(model: DTOClass<DATA, ENTITY>): PropertyBinder<DATA, ENTITY> {
       sourceModel = model
       initStatus = DTOInitStatus.PARTIAL_WITH_DATA
       return model.conf.propertyBinder
   }

   fun update(entity :ENTITY, mode: UpdateMode){
        propertyBinder.update(injectedDataModel, entity, mode)
        entityDAO = entity
        if(mode != UpdateMode.MODEL_TO_ENTNTY){
            id =  entity.id.value
        }
    }

    companion object{
        fun <DATA: DataModel, ENTITY: LongEntity>copyAsEntityDTO(
            injectedDataModel: DATA,
            dtoClass: DTOClass<DATA,ENTITY>
        ): EntityDTO<DATA,ENTITY> {
            return object : EntityDTO<DATA, ENTITY>(injectedDataModel) {}.apply {
                initialize(dtoClass)
            }
        }
    }
}
package po.db.data_service.models

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.binder.BindingContainer
import po.db.data_service.binder.BindingKeyBase
import po.db.data_service.binder.ChildContainer
import po.db.data_service.binder.PropertyBinder
import po.db.data_service.binder.UpdateMode
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DTOEntity
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.OperationsException


open class HostingDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
    injectedDataModel: DATA
): DTOContainerBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(injectedDataModel)
        where DATA : DataModel, ENTITY: LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY: LongEntity


abstract class CommonDTO<DATA, ENTITY>(
    injectedDataModel: DATA
): DTOContainerBase<DATA, ENTITY, DataModel, LongEntity>(injectedDataModel), DTOEntity<DATA, ENTITY>, Cloneable
        where DATA: DataModel , ENTITY: LongEntity {

    fun <CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity> copyAsHostingDTO(
    ): HostingDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY> {
        val result = object : HostingDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(injectedDataModel){}
        return result
    }
}


sealed class DTOContainerBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
   protected  val injectedDataModel : DATA
): DTOEntity<DATA, ENTITY>
        where DATA : DataModel, ENTITY: LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY: LongEntity{

    var onInitializationStatusChange : ((DTOContainerBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>)-> Unit)? = null
    var initStatus: DTOInitStatus = DTOInitStatus.UNINITIALIZED
        set(value){
            if(value!= field){
                field = value
                onInitializationStatusChange?.invoke(this)
            }
        }

    override var id : Long
        get(){return injectedDataModel.id}
        set(value) {injectedDataModel.id = value}

   private  var _sourceModel: DTOClass<DATA, ENTITY>? = null
   var sourceModel : DTOClass<DATA, ENTITY>
        get(){return  _sourceModel?: throw OperationsException(
            "Trying to access dtoModel property of CommonDTOV2 id :$id while undefined",
            ExceptionCodes.LAZY_NOT_INITIALIZED) }
        set(value){ _sourceModel = value}

   private var _entityDAO : ENTITY? = null
   override var entityDAO : ENTITY
        set(value){  _entityDAO = value }
        get(){return  _entityDAO?:throw OperationsException(
            "Entity uninitialized",
            ExceptionCodes.LAZY_NOT_INITIALIZED) }

    val isUnsaved : Boolean
        get(){
            return id == 0L
        }

    val propertyBinder: PropertyBinder<DATA,ENTITY> by lazy { initialize(sourceModel) }

    val bindings = mutableMapOf<BindingKeyBase, BindingContainer<DATA, ENTITY, *, *>>()

    fun toDataModel(): DATA =  this.injectedDataModel

    fun initialize(model: DTOClass<DATA, ENTITY>): PropertyBinder<DATA, ENTITY> {
       sourceModel = model
       initStatus = DTOInitStatus.PARTIAL_WITH_DATA
       return model.conf.propertyBinder
   }

    fun update(entity :ENTITY, mode: UpdateMode){
        propertyBinder.update(injectedDataModel, entity, mode)
        entityDAO = entity
        if(mode == UpdateMode.ENTITY_TO_MODEL || mode == UpdateMode.ENTITY_TO_MODEL_FORCED){
            id =  entity.id.value
        }
        initStatus = DTOInitStatus.INITIALIZED
    }

    fun update(dataModel: DATA, mode: UpdateMode){
        propertyBinder.update(dataModel, entityDAO, mode)
    }

    /**
     * Extracts complete dataModel with all sub child records
     *
     */
    fun getDataModel():DATA{
        return injectedDataModel
    }

   companion object{
        fun <DATA: DataModel, ENTITY: LongEntity>copyAsEntityDTO(
            injectedDataModel: DATA,
            dtoClass: DTOClass<DATA,ENTITY>
        ): CommonDTO<DATA,ENTITY> {
            return object : CommonDTO<DATA, ENTITY>(injectedDataModel) {}.apply {
                initialize(dtoClass)
            }
        }
    }
}
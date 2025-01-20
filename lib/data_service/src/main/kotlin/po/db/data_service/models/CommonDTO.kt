package po.db.data_service.models

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.binder.BindingKeyBase
import po.db.data_service.binder.OrdinanceType
import po.db.data_service.binder.PropertyBinder
import po.db.data_service.binder.UpdateMode
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.components.MultipleRepository
import po.db.data_service.dto.components.RepositoryBase
import po.db.data_service.dto.components.SingleRepository
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.OperationsException


class HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
    injectedDataModel: DATA
): DTOBase<DATA, ENTITY,  CHILD_DATA, CHILD_ENTITY>(injectedDataModel)
        where DATA : DataModel, ENTITY: LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY: LongEntity{

    val repositories = mutableMapOf<BindingKeyBase, RepositoryBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>>()

    var onUpdate: (()-> Unit)? = null
    var onUpdateFromEntity: ((ENTITY)-> Unit)? = null

    fun initializeRepositories(entity: ENTITY){
        repositories.values.forEach {
            it.initialize(entity)
        }
    }

    fun initializeRepositories(){
        repositories.values.forEach {
            it.initialize(getInjectedModel())
        }
    }

    fun updateRootRepositories(){
        sourceModel.daoService.saveNew(this)?.let {
            onUpdate?.invoke()
        }
    }

    fun compileDataModel(dataModel:DATA):DATA{
        repositories.values.forEach {


            when(it){
                is SingleRepository->{
                    it.binding.thisKey.childModel
                    it.submitSingleDataModel(dataModel)

                }

                is MultipleRepository->{


                }
            }
        }
        return injectedDataModel
    }

    companion object{
        fun <DATA : DataModel , ENTITY : LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity>createHosted(
            dataModel : DATA,
            dtoModel: DTOClass<DATA, ENTITY>,
        ): HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>
        {
            val hosted = HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(dataModel)
            hosted.initialize(dtoModel)
            return hosted
        }
    }

}

abstract class CommonDTO<DATA, ENTITY>(
    injectedDataModel: DATA
): DTOBase<DATA, ENTITY, DataModel, LongEntity>(injectedDataModel), Cloneable
        where DATA: DataModel , ENTITY: LongEntity
{
    var hostDTO  : HostDTO<DATA, ENTITY, *, *>? = null

    fun initializeRepositories(entity:ENTITY){
        hostDTO?.initializeRepositories(entity)
    }
    fun initializeRepositories(){
        hostDTO?.initializeRepositories()
    }
    fun updateRepositories(){
        hostDTO?.updateRootRepositories()
    }

    fun compileDataModel():DATA{
        hostDTO?.compileDataModel(getInjectedModel())?.let {
            return it
        }
        throw OperationsException("Data compilation failure", ExceptionCodes.INVALID_DATA)
    }
}

sealed class DTOBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
   protected  val injectedDataModel : DATA
) where DATA : DataModel, ENTITY: LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY: LongEntity{

    var onInitializationStatusChange : ((DTOBase<DATA, ENTITY, *, *>)-> Unit)? = null
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
            "Trying to access dtoModel property of DTOContainerBase id :$id while undefined",
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

    fun getInjectedModel(): DATA =  this.injectedDataModel


    fun initialize(model: DTOClass<DATA, ENTITY>): PropertyBinder<DATA, ENTITY> {
       sourceModel = model
       initStatus = DTOInitStatus.PARTIAL_WITH_DATA
       return model.conf.propertyBinder
   }

    fun update(entity : ENTITY, mode: UpdateMode){
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


   companion object{

       fun <DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>  CommonDTO<DATA, ENTITY>.copyAsHostingDTO()
        : HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>
            where  DATA: DataModel, ENTITY: LongEntity,  CHILD_DATA: DataModel, CHILD_ENTITY : LongEntity
       {
           return HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(this.injectedDataModel).also {
               it.initialize(this.sourceModel)
               if(this.initStatus == DTOInitStatus.INITIALIZED){
                   it.update(this.entityDAO, UpdateMode.ENTITY_TO_MODEL)
               }
           }
       }

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
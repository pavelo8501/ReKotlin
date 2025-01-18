package po.db.data_service.models

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.binder.BindingContainer
import po.db.data_service.binder.BindingKeyBase
import po.db.data_service.binder.MultipleChildContainer
import po.db.data_service.binder.PropertyBinder
import po.db.data_service.binder.UpdateMode
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.components.RepositoryBase
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.OperationsException


class HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
    injectedDataModel: DATA
): DTOContainerBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(injectedDataModel)
        where DATA : DataModel, ENTITY: LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY: LongEntity{

    val repositories = mutableMapOf<BindingKeyBase, RepositoryBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>>()

    var onInitHostedRequested: ((entity: ENTITY)-> Unit)? = null
    fun initHosted(entity: ENTITY){
        onInitHostedRequested?.invoke(entity)
    }


    fun initHosted(
        dataModel : DATA,
        block: ((ENTITY)-> Unit)? = null): HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>?{
            sourceModel.factory.createEntityDto(dataModel)?.let {newDto->
                val hosted =  newDto.copyAsHostingDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>()
                hosted.initialize(sourceModel)
                sourceModel.daoService.saveNew(newDto, block)
                bindings.values.forEach {binding->
                    when(binding.thisKey){
                        is BindingKeyBase.OneToMany<*, *> -> {
                            binding as MultipleChildContainer
                            binding.createFromDataModel(newDto)
                        }
                        else -> {}
                    }
                }
                return hosted
            }
        return null
    }


    fun intiChildByEntity(){

        bindings.values.forEach {

        }

//        childModel.apply {
//            byProperty.get(parentDto.entityDAO).forEach {
//                childModel.initHosted(it)
//                val childDto = initDTO(it)
//                if(childDto != null){
//                    repository.add(childDto)
//                }else{
//                    TODO("Actions to be taken on child dto creation null")
//                }
//            }
//        }
//        parentDto.bindings[thisKey] = this
//        repository.clear()
//
//        }
    }
}

abstract class CommonDTO<DATA, ENTITY>(
    injectedDataModel: DATA
): DTOContainerBase<DATA, ENTITY, DataModel, LongEntity>(injectedDataModel), Cloneable
        where DATA: DataModel , ENTITY: LongEntity
{
    var hostDTO  : HostDTO<DATA, ENTITY, *, *>? = null

    fun initHosted(){
        hostDTO?.initHosted(entityDAO)
    }

}

sealed class DTOContainerBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
   protected  val injectedDataModel : DATA
) where DATA : DataModel, ENTITY: LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY: LongEntity{

    var onInitializationStatusChange : ((DTOContainerBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>)-> Unit)? = null
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

    val bindings = mutableMapOf<BindingKeyBase, BindingContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>>()

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
    fun extractDataModel():DATA{
        return injectedDataModel
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
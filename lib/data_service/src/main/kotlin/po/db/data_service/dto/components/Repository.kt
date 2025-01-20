package po.db.data_service.dto.components

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.binder.BindingContainer
import po.db.data_service.binder.BindingKeyBase
import po.db.data_service.binder.MultipleChildContainer
import po.db.data_service.binder.SingleChildContainer
import po.db.data_service.binder.UpdateMode
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.DTOBase.Companion.copyAsHostingDTO
import po.db.data_service.models.HostDTO
import kotlin.collections.set

class SingleRepository<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
    parent :  HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>,
    childModel : DTOClass<CHILD_DATA, CHILD_ENTITY>,
    bindingKey : BindingKeyBase.OneToOne<CHILD_DATA, CHILD_ENTITY>,
    val binding : SingleChildContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>
): RepositoryBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(parent, childModel,  bindingKey, binding)
        where  DATA : DataModel, ENTITY : LongEntity, CHILD_DATA: DataModel, CHILD_ENTITY: LongEntity
{
    override val repoName: String =  "Repository ${parent.sourceModel.className}/Single"

//    fun initialize(){
//        parent.subscribeOnInitByData(parent) {
//            extractDataModel(it.toDataModel())?.let {childData ->
//                createHosted(childData).let { hosted ->
//                        hosted.setChildBindings()
//                        childModel.daoService.saveNew(hosted) { childEntity ->
//                            binding.referencedOnProperty.set(childEntity, it.entityDAO)
//                        }
//                        dtoList.add(hosted)
//                    }
//                }
//        }
//    }

    override fun  setReferenced(childEntity:CHILD_ENTITY, parentEntity:ENTITY){
        binding.referencedOnProperty.set(childEntity, parentEntity)
    }

    override fun getReferences(parentEntity: ENTITY): List<CHILD_ENTITY> {
        binding.byProperty.get(parentEntity)?.let {
            return  listOf<CHILD_ENTITY>(it)
        }?: return emptyList()
    }

    override fun extractDataModel(dataModel:DATA): List<CHILD_DATA>{
        val result =  childModel.factory.extractDataModel(binding.sourceProperty, dataModel)
        return if(result!=null){
            listOf<CHILD_DATA>(result)
        }else{
            emptyList()
        }
    }

}

class MultipleRepository<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
    parent : HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>,
    childModel : DTOClass<CHILD_DATA, CHILD_ENTITY>,
    bindingKey : BindingKeyBase.OneToMany<CHILD_DATA, CHILD_ENTITY>,
    val binding : MultipleChildContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>
): RepositoryBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(parent, childModel, bindingKey, binding)
    where  DATA : DataModel, ENTITY : LongEntity, CHILD_DATA: DataModel, CHILD_ENTITY: LongEntity
{

    override val repoName: String =  "Repository ${parent.sourceModel.className}/Multiple"

    override fun setReferenced(childEntity:CHILD_ENTITY, parentEntity:ENTITY){
        binding.referencedOnProperty.set(childEntity, parentEntity)
    }

    override fun getReferences(parentEntity: ENTITY): List<CHILD_ENTITY> {
        binding.byProperty.get(parentEntity).let {
            return  it.toList()
        }
    }

    override fun extractDataModel(dataModel:DATA): List<CHILD_DATA>
            = childModel.factory.extractDataModel(binding.sourceProperty, dataModel)
}

sealed class RepositoryBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
    protected val parent : HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>,
    protected val childModel : DTOClass<CHILD_DATA, CHILD_ENTITY>,
    protected val bindingKey : BindingKeyBase,
    private val binding : BindingContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>
) where DATA : DataModel, ENTITY : LongEntity, CHILD_DATA: DataModel, CHILD_ENTITY: LongEntity{

    abstract val repoName : String

    abstract fun extractDataModel(dataModel:DATA):List<CHILD_DATA>
    abstract fun setReferenced(childEntity:CHILD_ENTITY, parentEntity:ENTITY)
    abstract fun getReferences(parentEntity:ENTITY): List<CHILD_ENTITY>

    var initialized: Boolean = false
    val dtoList = mutableListOf<HostDTO<CHILD_DATA, CHILD_ENTITY,DATA, ENTITY>>()

    val factory: Factory<CHILD_DATA, CHILD_ENTITY>
        get(){return  childModel.factory }

    val daoService:  DAOService<DATA, ENTITY>
        get(){return  parent.sourceModel.daoService }

    init {
        parent.onUpdate={
            println("OnUpdate Callback invoked by parent in ${repoName}")
            onInitHostedByData.forEach {
                println("Invoking stored by ${it.first.sourceModel.className} Fn in ${repoName}")
                it.second()
            }
        }

        parent.onUpdateFromEntity={
            println("OnUpdateFromEntity Callback invoked by parent in ${repoName}")
        }
    }

    private val onInitHostedByData = mutableListOf<
            Pair<HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>,
                        ()-> Unit>>()

    private fun subscribeOnInitByData(
        subscriber: HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>,
        callback: ()-> Unit){
        onInitHostedByData.add(Pair(subscriber, callback))
    }

    private var onInitHostedByEntity = mutableListOf<
            Pair<HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>,
                (ENTITY)-> Unit>>()

    fun subscribeOnInitByEntity(
        subscriber: HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>,
        callback:  (entity: ENTITY)-> Unit){
        onInitHostedByEntity.add(Pair(subscriber, callback))
    }

    /**
     * Propagate a call to the parent repository.
     */
    fun propagateOnInitByDataToChild(
        childDTO: HostDTO<CHILD_DATA, CHILD_ENTITY,DATA, ENTITY>){
        childDTO.onUpdate?.invoke()
    }

    fun propagateOnInitByEntity(
        entity:CHILD_ENTITY,
        childDTO: HostDTO<CHILD_DATA, CHILD_ENTITY,DATA, ENTITY>){
        childDTO.onUpdateFromEntity?.invoke(entity)
    }

    fun initialize(entity:ENTITY){
        getReferences(entity).forEach { childEntity ->
            factory.createDataModel().let { dataModel ->
               createHosted(dataModel).let { hosted ->
                   hosted.update(childEntity, UpdateMode.ENTITY_TO_MODEL)
                   dtoList.add(hosted)
                   hosted.initializeRepositories(hosted.entityDAO)
               }
            }
        }
        initialized = true
        println("Repository initialized for ${parent.sourceModel.className} with id ${parent.toDataModel().id}")
        println("ByData Subscriptions count ${onInitHostedByData.count()}")
        parent.repositories.values.forEach {
            it.subscribeOnInitByEntity(parent) {
                getReferences(entity).forEach { childEntity ->
                    factory.createDataModel().let { dataModel ->
                        createHosted(dataModel).let { hosted ->
                            propagateOnInitByEntity(childEntity, hosted)
                        }
                    }
                }
            }
        }
    }

    fun initialize(dataModel:DATA){
        extractDataModel(dataModel).forEach { childData ->
            createHosted(childData).let { hosted ->
                dtoList.add(hosted)
                childModel.bindings.values.forEach {
                    it.applyBindingToHost(hosted)
                    hosted.initializeRepositories()
                }
            }
        }
        initialized = true
        println("Repository initialized for ${parent.sourceModel.className} with id ${parent.toDataModel().id}")
        println("ByData Subscriptions count ${onInitHostedByData.count()}")
        parent.repositories.values.forEach {
            it.subscribeOnInitByData(parent){
                dtoList.forEach { dto ->
                    dto.sourceModel.daoService.saveNew(dto){
                        setReferenced(it, parent.entityDAO)
                    }
                    propagateOnInitByDataToChild(dto)
                }
            }
        }
    }

    protected fun createHosted(childData : CHILD_DATA):HostDTO<CHILD_DATA, CHILD_ENTITY, DATA, ENTITY>{
       return HostDTO.createHosted<CHILD_DATA, CHILD_ENTITY, DATA, ENTITY>(childData, childModel)
    }


}
package po.db.data_service.dto.components

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.binder.BindingContainer
import po.db.data_service.binder.BindingKeyBase
import po.db.data_service.binder.MultipleChildContainer
import po.db.data_service.binder.SingleChildContainer
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.HostDTO

class SingleRepository<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
    parent :  HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>,
    childModel : DTOClass<CHILD_DATA, CHILD_ENTITY>,
    bindingKey : BindingKeyBase.OneToOne<CHILD_DATA, CHILD_ENTITY>,
    val binding : SingleChildContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>
): RepositoryBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(parent, childModel,  bindingKey, binding)
        where  DATA : DataModel, ENTITY : LongEntity, CHILD_DATA: DataModel, CHILD_ENTITY: LongEntity
{
    init {
        parent.subscribeOnInitByData {
            extractDataModel(it.toDataModel())?.let {childData ->
                createHosted(childData).let { hosted ->
                        hosted.setChildBindings()
                        childModel.daoService.saveNew(hosted) { childEntity ->
                            binding.referencedOnProperty.set(childEntity, it.entityDAO)
                        }
                        dtoList.add(hosted)
                    }
                }
        }
    }

    private fun extractDataModel(dataModel:DATA)
            = childModel.factory.extractDataModel(binding.sourceProperty, dataModel)

}

class MultipleRepository<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
    parent : HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>,
    childModel : DTOClass<CHILD_DATA, CHILD_ENTITY>,
    bindingKey : BindingKeyBase.OneToMany<CHILD_DATA, CHILD_ENTITY>,
    val binding : MultipleChildContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>
): RepositoryBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(parent, childModel, bindingKey, binding)
    where  DATA : DataModel, ENTITY : LongEntity, CHILD_DATA: DataModel, CHILD_ENTITY: LongEntity
{

    init{
        parent.subscribeOnInitByEntity {entity->
            binding.byProperty.get(entity).forEach { childEntity ->

//                childModel.initDTO(childEntity)?.let { dto ->
//                    val hosted = dto.copyAsHostingDTO<CHILD_DATA, CHILD_ENTITY, DATA, ENTITY>()
//                    hosted.setChildBindings()
//                    dto.initHostedFromDb()
//                    dtoList.add(hosted)
//
//                }
            }
        }
        parent.subscribeOnInitByData {caller->
            extractDataModel(caller.toDataModel()).forEach { childData ->
                createHosted(childData).let { hosted ->
                    hosted.setChildBindings()
                    childModel.daoService.saveNew(hosted) { childEntity ->
                        binding.referencedOnProperty.set(childEntity, caller.entityDAO)
                    }
                    dtoList.add(hosted)
                }
            }
        }
    }

    private fun extractDataModel(dataModel:DATA)
            = childModel.factory.extractDataModel(binding.sourceProperty, dataModel)

}

sealed class RepositoryBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
    protected val parent : HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>,
    protected val childModel : DTOClass<CHILD_DATA, CHILD_ENTITY>,
    protected val bindingKey : BindingKeyBase,
    binding : BindingContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>
) where DATA : DataModel, ENTITY : LongEntity, CHILD_DATA: DataModel, CHILD_ENTITY: LongEntity{

   val dtoList = mutableListOf<HostDTO<CHILD_DATA, CHILD_ENTITY, DATA, ENTITY>>()

    protected fun createHosted(childData : CHILD_DATA):HostDTO<CHILD_DATA, CHILD_ENTITY, DATA, ENTITY>{
       return HostDTO.createHosted<CHILD_DATA, CHILD_ENTITY, DATA, ENTITY>(childData, childModel)
    }

    val factory: Factory<CHILD_DATA, CHILD_ENTITY>
        get(){return  childModel.factory }

}
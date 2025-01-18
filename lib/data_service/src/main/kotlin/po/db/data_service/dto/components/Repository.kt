package po.db.data_service.dto.components

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.binder.BindingContainer
import po.db.data_service.binder.BindingKeyBase
import po.db.data_service.binder.MultipleChildContainer
import po.db.data_service.binder.SingleChildContainer
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.DTOContainerBase.Companion.copyAsHostingDTO
import po.db.data_service.models.HostDTO


class SingleRepository<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
    parent : HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>,
    childModel : DTOClass<CHILD_DATA, CHILD_ENTITY>,
    bindingKey : BindingKeyBase,
    binding : SingleChildContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>
): RepositoryBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(parent, childModel,  bindingKey, binding)
        where  DATA : DataModel, ENTITY : LongEntity, CHILD_DATA: DataModel, CHILD_ENTITY: LongEntity
{
    val dto = mutableListOf<HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>>()
}

class MultipleRepository<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
    parent : HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>,
    childModel : DTOClass<CHILD_DATA, CHILD_ENTITY>,
    bindingKey : BindingKeyBase,
    binding : MultipleChildContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>
): RepositoryBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(parent, childModel, bindingKey, binding)
    where  DATA : DataModel, ENTITY : LongEntity, CHILD_DATA: DataModel, CHILD_ENTITY: LongEntity
{

}

sealed class RepositoryBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
    private val parent : HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>,
    private val childModel : DTOClass<CHILD_DATA, CHILD_ENTITY>,
    private val bindingKey : BindingKeyBase,
    private val binding : BindingContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>
) where DATA : DataModel, ENTITY : LongEntity, CHILD_DATA: DataModel, CHILD_ENTITY: LongEntity{

    val dtoList = mutableListOf<HostDTO<CHILD_DATA, CHILD_ENTITY, DATA, ENTITY>>()

    init {
        when(binding){
            is MultipleChildContainer -> {
                parent.subscribeOnInitHostedByEntity {
                    binding.byProperty.get(it).forEach { childEntity ->
                        childModel.initDTO(childEntity)?.let { dto ->
                            val hosted = dto.copyAsHostingDTO<CHILD_DATA, CHILD_ENTITY, DATA, ENTITY>()
                            dtoList.add(hosted)
                        }
                    }
                }
                parent.subscribeOnInitHostedByData {
                    childModel.factory.extractDataModel(binding.sourceProperty, it.toDataModel())
                        .forEach { childData ->
                            childModel.initDTO(childData)?.let { dto ->
                                childModel.daoService.saveNew(dto) { childEntity ->
                                    binding.referencedOnProperty.set(childEntity, it.entityDAO)
                                }
                                val hosted = dto.copyAsHostingDTO<CHILD_DATA, CHILD_ENTITY, DATA, ENTITY>()
                                dtoList.add(hosted)
                            }
                        }
                }
            }
            is SingleChildContainer -> {
                parent.subscribeOnInitHostedByData {
                    childModel.factory.extractDataModel(binding.sourceProperty, it.toDataModel())
                        ?.let {childData ->
                            childModel.initDTO(childData)?.let { dto ->
                                childModel.daoService.saveNew(dto) { childEntity ->
                                    binding.referencedOnProperty.set(childEntity, it.entityDAO)
                                }
                                val hosted = dto.copyAsHostingDTO<CHILD_DATA, CHILD_ENTITY, DATA, ENTITY>()
                                dtoList.add(hosted)
                            }
                        }
                }
            }
        }
    }
}
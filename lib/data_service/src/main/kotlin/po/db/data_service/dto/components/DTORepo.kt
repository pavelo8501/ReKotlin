package po.db.data_service.dto.components

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.binder.BindingKey
import po.db.data_service.dto.DTOClass
import po.db.data_service.models.CommonDTO
import po.db.data_service.models.HostableDTO

class DTORepo<ENTITY>(
    val hostDTOClass: DTOClass<ENTITY>,
    val key: BindingKey,
    val parentId: Long
) where ENTITY : LongEntity {
    private val entities = mutableMapOf<Long,CommonDTO>()
    fun add(dto:  CommonDTO): Boolean {
        if (entities.containsKey(dto.getId())){
            return false
        }else{
            entities[dto.getId()] = dto
            return true
        }
    }

    fun get(id:Long):CommonDTO?{
        return entities[id]
    }
    fun get(dto:CommonDTO):CommonDTO?{
        return get(dto.getId())
    }
    fun getAll():List<CommonDTO>{
        return entities.values.toList()
    }
    fun getExistent(compareTo: List<CommonDTO>):List<CommonDTO>{
        return entities.filterKeys { key ->
            compareTo.any { it.getId() == key }
        }.values.toList()
    }
}


class HostableRepo<ENTITY>(
    override val hostDTOClass: DTOClass<ENTITY>,
    override val key: BindingKey,
    override val parentId: Long
) : DTORepoBase<ENTITY>() where ENTITY : LongEntity {
    private val entities = mutableMapOf<Long, HostableDTO<ENTITY>>()


    fun <PARENT: LongEntity>update(parentDaoEntity: PARENT, body : DTOClass<ENTITY>.(PARENT) -> Unit ){
        hostDTOClass.body(parentDaoEntity)

    }
}

sealed class DTORepoBase<ENTITY>() where ENTITY : LongEntity{
    abstract val hostDTOClass: DTOClass<ENTITY>
    abstract val key: BindingKey
    abstract val parentId: Long

    private val entities = mutableMapOf<Long,HostableDTO<ENTITY>>()

    fun add(dto:  HostableDTO<ENTITY> ): Boolean {
        if (entities.containsKey(dto.getId())){
            return false
        }else{
            entities[dto.getId()] = dto
            return true
        }
    }

    fun get(id:Long):HostableDTO<ENTITY>?{
        return entities[id]
    }
    fun get(dto:CommonDTO):HostableDTO<ENTITY>?{
        return get(dto.getId())
    }

    fun getAll():List<HostableDTO<ENTITY>>{
        return entities.values.toList()
    }

    fun getExistent(compareTo: List<HostableDTO<ENTITY>>):List<HostableDTO<ENTITY>>{
        return entities.filterKeys { key ->
            compareTo.any { it.getId() == key }
        }.values.toList()
    }

}
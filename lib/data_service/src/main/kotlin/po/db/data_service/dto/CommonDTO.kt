package po.db.data_service.dto

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.exceptions.ExceptionCodes.NOT_INITIALIZED
import po.db.data_service.exceptions.InitializationException

abstract class CommonDTO<DATA_MODEL : DataModel, ENTITY : LongEntity>(
    private val injectedDataModel : DATA_MODEL
):DTOEntityMarker<DATA_MODEL, ENTITY>, Cloneable{

    override var id:Long = 0L

    override val dataModelClassName: String = ""

    abstract fun mapToEntity(entity: ENTITY): ENTITY
    abstract fun mapFromEntity(entity: ENTITY): DATA_MODEL

    public override fun clone(): DATA_MODEL = this.clone()

    fun updateEntity(entity: ENTITY): ENTITY {
        return mapToEntity(entity)
    }

    private var _entityDAO : ENTITY? = null
    val entityDAO : ENTITY
        get(): ENTITY {
            return _entityDAO?: throw InitializationException("Trying to access database daoEntity associated with ${this.dataModelClassName}", NOT_INITIALIZED)
        }

    override fun setEntityDAO(entity :ENTITY){
        _entityDAO = entity as ENTITY
        if(id != entity.id.value){
            id = entity.id.value

        }
    }

    fun toDTO(): DATA_MODEL =  this.injectedDataModel
}
package po.db.data_service.models

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.dto.interfaces.DTOEntityMarker
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes.NOT_INITIALIZED
import po.db.data_service.exceptions.InitializationException


abstract class CommonDTO<DATA_MODEL, ENTITY>(
    private val injectedDataModel : DATA_MODEL): DTOEntityMarker<DATA_MODEL, ENTITY>, Cloneable where DATA_MODEL : DataModel, ENTITY : LongEntity{

    override var id:Long = 0L

    override val dataModelClassName: String = ""

    public override fun clone(): DATA_MODEL = this.clone()

    fun toDTO(): DATA_MODEL =  this.injectedDataModel

    private var _entityDAO : ENTITY? = null
    val entityDAO : ENTITY
        get(): ENTITY {
            return _entityDAO?: throw InitializationException("Trying to access database daoEntity associated with ${this.dataModelClassName}", NOT_INITIALIZED)
        }

}
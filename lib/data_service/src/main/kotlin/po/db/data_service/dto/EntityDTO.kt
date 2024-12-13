package po.db.data_service.dto

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.exceptions.ExceptionCodes.*
import po.db.data_service.exceptions.InitializationException


abstract class AbstractDTOModel<DATA_MODEL: DataModel, ENTITY : LongEntity>(): DTOEntityMarker<DATA_MODEL, ENTITY>{

    abstract override var id : Long
    override val dataModelClassName: String = ""

    private var _dataModel : DATA_MODEL? = null
    val dataModel : DATA_MODEL
        get(){
            return _dataModel?: throw InitializationException("dataModel uninitialized", NOT_INITIALIZED)
        }

    private var dtoModel : DTOClass<DATA_MODEL, ENTITY>? = null

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

    fun asDataModel():DATA_MODEL{
        return dataModel
    }

    fun asDTOEntity(): AbstractDTOModel<DATA_MODEL , ENTITY>{
        return this
    }

    val initialized : Boolean
        get(){
            return _entityDAO != null
        }

    constructor(dataModelObject : DTOClass<DATA_MODEL, ENTITY>) : this(){
        dtoModel = dataModelObject
    }

    private val childClass: AbstractDTOModel<DATA_MODEL, ENTITY>
        get (){
            return this
        }
}
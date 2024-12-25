package po.db.data_service.models

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.binder.PropertyBinder
import po.db.data_service.binder.UpdateMode
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DTOEntity
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.OperationsException
import kotlin.reflect.KMutableProperty1

abstract class CommonDTO(private val injectedDataModel : DataModel, val  childDataSource: List<DataModel>? = null): DTOEntity, Cloneable {

    override var id:Long = 0L
    private var _dtoModel : DTOClass<*>? = null
    val dtoModel : DTOClass<*>
        get(){return  _dtoModel?:
        throw OperationsException("Trying to access dtoModel property of CommonDTOV2 id :$id while undefined",
            ExceptionCodes.LAZY_NOT_INITIALIZED) }


    private var _entityDAO : LongEntity? = null
        set(value){
            if(value!= null){
                field = value
               // id = value.id.value
            }
        }
    fun <ENTITY: LongEntity>getEntityDAO():ENTITY{
        @Suppress("UNCHECKED_CAST")
        return (_entityDAO as ENTITY)?: throw OperationsException("Reading entityDAO while undefined", ExceptionCodes.LAZY_NOT_INITIALIZED)
    }

    private var propertyBinder: PropertyBinder? = null

    val childDTOs = mutableListOf<CommonDTO>()

    override fun initialize(binder : PropertyBinder?, dataModel : DataModel?){
        propertyBinder = binder
        id = injectedDataModel.id
    }

    public override fun clone(): DataModel = this.clone()

    fun toDTO(): DataModel =  this.injectedDataModel

    fun updateDAO(daoEntity: LongEntity):LongEntity?{
        if(propertyBinder != null){
            propertyBinder!!.updateProperties(injectedDataModel, daoEntity, UpdateMode.MODEL_TO_ENTITY)
            _entityDAO = daoEntity
            return daoEntity
        }
        //Issue warning
            return null
    }

    fun updateDTO (entity :LongEntity, dtoModel : DTOClass<*>){
        this._dtoModel = dtoModel
        _entityDAO = entity
        id = entity.id.value
        if(propertyBinder!= null){
            propertyBinder!!.updateProperties(injectedDataModel, entity, UpdateMode.ENTITY_TO_MODEL )
        }else{
            //Issue Warning
        }
    }
}
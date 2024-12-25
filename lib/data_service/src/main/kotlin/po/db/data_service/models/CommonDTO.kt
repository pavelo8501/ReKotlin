package po.db.data_service.models

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.binder.PropertyBinderV2
import po.db.data_service.binder.PropertyBindingV2
import po.db.data_service.binder.UpdateMode
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DTOEntity
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.OperationsException

abstract class CommonDTO(private val injectedDataModel : DataModel): DTOEntity, Cloneable {

    override var id:Long = 0L
    private var _dtoModel : DTOClass<*>? = null
    val dtoModel : DTOClass<*>
        get(){return  _dtoModel?:
        throw OperationsException("Trying to access dtoModel property of CommonDTOV2 id :$id while undefined",
            ExceptionCodes.LAZY_NOT_INITIALIZED) }
    private var entityDAO : LongEntity? = null

    private var propertyBinder: PropertyBinder? = null

    override fun initialize(binder : PropertyBinder, dataModel : DataModel?){
        propertyBinder = binder

    }

    public override fun clone(): DataModel = this.clone()

    fun toDTO(): DataModel =  this.injectedDataModel

    fun newDAO(daoEntity: LongEntity):LongEntity?{
        if(propertyBinder != null){
            propertyBinder!!.update(injectedDataModel, daoEntity, UpdateMode.MODEL_TO_ENTITY)
            entityDAO = daoEntity
            return daoEntity
        }
        //Issue warning
            return null
    }



    fun setEntityDAO(entity :LongEntity, dtoModel : DTOClass<*>){
        this._dtoModel = dtoModel
        entityDAO = entity
        if(id != entity.id.value){
            id = entity.id.value
            dtoModel.update(toDTO(), entity)
            //dtoModel.loadChildren<>(entity)
        }
    }

}
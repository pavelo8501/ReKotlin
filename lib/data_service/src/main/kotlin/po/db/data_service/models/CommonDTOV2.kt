package po.db.data_service.models

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DTOModelV2
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.OperationsException

abstract class CommonDTOV2(private val injectedDataModel : DataModel): DTOModelV2, Cloneable {

    override var id:Long = 0L
    private var _dtoModel : DTOClass? = null
    val dtoModel : DTOClass
        get(){return  _dtoModel?:
        throw OperationsException("Trying to access dtoModel property of CommonDTOV2 id :$id while undefined",
            ExceptionCodes.LAZY_NOT_INITIALIZED) }
    private var entityDAO : LongEntity? = null

    public override fun clone(): DataModel = this.clone()

    fun toDTO(): DataModel =  this.injectedDataModel

    fun setEntityDAO(entity :LongEntity, dtoModel : DTOClass){
        this._dtoModel = dtoModel
        entityDAO = entity
        if(id != entity.id.value){
            id = entity.id.value
            dtoModel.update(toDTO(), entity)
            //dtoModel.loadChildren<>(entity)
        }
    }

}
package po.db.data_service.models

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.DTOClassV2
import po.db.data_service.dto.interfaces.DTOModelV2
import po.db.data_service.dto.interfaces.DataModel

abstract class CommonDTOV2(private val injectedDataModel : DataModel): DTOModelV2, Cloneable {

    override var id:Long = 0L
    private var dtoModel : DTOClassV2? = null
    private var entityDAO : LongEntity? = null

    public override fun clone(): DataModel = this.clone()

    fun toDTO(): DataModel =  this.injectedDataModel

    fun loadChildren(){
        dtoModel.
    }

    fun setEntityDAO(entity :LongEntity, dtoModel : DTOClassV2){
        this.dtoModel = dtoModel
        entityDAO = entity
        if(id != entity.id.value){
            id = entity.id.value
            dtoModel.update(toDTO(), entity)
        }
    }

}
package po.db.data_service.models

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.binder.PropertyBinder
import po.db.data_service.binder.UpdateMode
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DTOEntity
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.OperationsException



abstract class EntityDTO<DATA, ENTITY>(
    injectedDataModel : DATA
): DTOContainerBase<DATA, ENTITY>(injectedDataModel),  DTOEntity<DATA, LongEntity>, Cloneable where DATA: DataModel , ENTITY : LongEntity {

    final override fun initialize(model: DTOClass<DATA, ENTITY>): PropertyBinder<DATA, ENTITY> {
        sourceModel = model
        return model.conf.propertyBinder
    }
}

abstract class CommonDTO<DATA>(
    injectedDataModel : DATA
): DTOContainerBase<DATA, LongEntity>(injectedDataModel),  DTOEntity<DATA, LongEntity>, Cloneable where DATA: DataModel {

    companion object{
        val childCompanionList = mutableListOf<DTOClass.Companion>()
        fun getThisCommonDTO():CommonDTO.Companion{
            return this
        }
        fun <E: LongEntity>getChildCompanion(): DTOClass.Companion{
            return DTOClass.Companion
        }
    }
    val childDTOs = mutableListOf<CommonDTO<DATA>>()
    public override fun clone(): DataModel = this.clone()
}

sealed class DTOContainerBase<DATA, ENTITY>(
    val injectedDataModel : DATA
) where DATA : DataModel, ENTITY: LongEntity{

    var id : Long
        get(){return injectedDataModel.id}
        set(value) {injectedDataModel.id = value}

    private  var _sourceModel: DTOClass<DATA, ENTITY>? = null
    var sourceModel : DTOClass<DATA,ENTITY>
        get(){return  _sourceModel?: throw OperationsException("Trying to access dtoModel property of CommonDTOV2 id :$id while undefined",
            ExceptionCodes.LAZY_NOT_INITIALIZED) }
        set(value){ _sourceModel = value}

    private var _entityDAO : ENTITY? = null
    var entityDAO : ENTITY
        set(value){  _entityDAO = value }
        get(){return  _entityDAO?:throw OperationsException("Entity uninitialized", ExceptionCodes.LAZY_NOT_INITIALIZED) }

    val propertyBinder: PropertyBinder<DATA,ENTITY> by lazy { initialize(sourceModel) }

    fun toDataModel(): DataModel =  this.injectedDataModel

   abstract fun initialize(model: DTOClass<DATA, ENTITY>):PropertyBinder<DATA,ENTITY>

   fun updateDTO (entity :ENTITY, mode: UpdateMode){
        entityDAO = entity
        propertyBinder.update(injectedDataModel, entity, mode)
        if(mode != UpdateMode.MODEL_TO_ENTNTY){
            id =  entity.id.value
        }
    }

}
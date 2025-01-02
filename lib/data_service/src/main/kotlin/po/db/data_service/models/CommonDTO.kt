package po.db.data_service.models

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.binder.PropertyBinder
import po.db.data_service.binder.UpdateMode
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DTOEntity
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.OperationsException

abstract class CommonDTO<DATA>(
    val injectedDataModel : DATA,
    val childDataSource : List<DATA>? =null
): DTOContainerBase<DATA>(injectedDataModel,  childDataSource),  DTOEntity<DATA, LongEntity>, Cloneable where DATA: DataModel {

    override var id:Long = 0L
    private var _dtoModel : DTOClass<DATA,*>? = null
    val dtoModel : DTOClass<DATA,*>
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
        return (_entityDAO as ENTITY)
    }

    companion object{
        val childCompanionList = mutableListOf<DTOClass.Companion>()
        fun reportToMe(childCompanion: DTOClass<*,*>){
            when(childCompanion::class){
                is DTOClass<*,*> ->  {
                    val a =10

                    val b = 39
                }
            }
            println("${childCompanion::class} Captured")
        }
        override fun hashCode(): Int {
            return super.hashCode()
        }
        override fun equals(other: Any?): Boolean {
            return super.equals(other)
        }
        fun getThisCommonDTO():CommonDTO.Companion{
            return this
        }
        fun <E: LongEntity>getChildCompanion(): DTOClass.Companion{
            return DTOClass.Companion
        }
    }

    private var propertyBinder: PropertyBinder<DATA,LongEntity,*>? = null

    val childDTOs = mutableListOf<CommonDTO<DATA>>()

    override fun initialize(binder : PropertyBinder<DATA, LongEntity,*>?, dataModel : DATA?){
        propertyBinder = binder
        id = injectedDataModel.id
    }

    public override fun clone(): DataModel = this.clone()

    fun toDTO(): DataModel =  this.injectedDataModel

    fun <ENTITY: LongEntity>updateDAO(daoEntity: ENTITY):LongEntity?{
        if(propertyBinder != null){
            propertyBinder!!.updateProperties(injectedDataModel, daoEntity, UpdateMode.MODE_TO_ENTNTITY)
            _entityDAO = daoEntity
            return daoEntity
        }
        //Issue warning
            return null
    }

    fun <ENTITY: LongEntity>updateDTO (entity :ENTITY, dtoModel : DTOClass<DATA,ENTITY>){
        this._dtoModel = dtoModel
        _entityDAO = entity
        id = entity.id.value
        if(propertyBinder!= null){
            propertyBinder!!.updateProperties(injectedDataModel, entity, UpdateMode.ENTNTITY_TO_MODEL )
        }else{
            //Issue Warning
        }
    }
}

sealed class DTOContainerBase<DATA>(injectedDataModel : DataModel,   childDataSource: List<DataModel>? = null){

}
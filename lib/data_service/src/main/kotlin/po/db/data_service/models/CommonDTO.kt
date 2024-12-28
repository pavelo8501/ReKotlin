package po.db.data_service.models

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.binder.BindingContainer
import po.db.data_service.binder.DataSource
import po.db.data_service.binder.PropertyBinder
import po.db.data_service.binder.UpdateMode
import po.db.data_service.common.enums.InitStatus
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.components.DTORepo
import po.db.data_service.dto.components.safeCast
import po.db.data_service.dto.interfaces.DTOEntity
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.OperationsException

abstract class CommonDTOClass<DATA: DataModel>(){

}

abstract class CommonDTO(override val injectedDataModel : DataModel, val childDataSource: List<DataModel>? = null): DTOEntity, Cloneable {

    var initStatus: InitStatus = InitStatus.UNINITIALIZED
        private set

    val errors = mutableListOf<String>()

    fun addError(msg:String){
        errors.add(msg)
        initStatus = InitStatus.INIT_FAILED
    }

    override fun getId():Long{
       return injectedDataModel.id
    }
    override fun setId(value:Long){
        injectedDataModel.id = value
    }

    private var _dtoModel : DTOClass<*>? = null
    val dtoModel : DTOClass<*>
        get(){return  _dtoModel?:
        throw OperationsException("Trying to access dtoModel property of CommonDTOV2 id :${getId()} while undefined",
            ExceptionCodes.LAZY_NOT_INITIALIZED) }

    val repos = mutableListOf<DTORepo<*,*>>()
    fun <ENTITY: LongEntity, CHILD_ENTITY: LongEntity > addRepository(repo: DTORepo<ENTITY,CHILD_ENTITY>){
        repos.add(repo)
    }

    private var _entityDAO : LongEntity? = null
        set(value){
            if(value!= null){
                field = value
               // id = value.id.value
            }
        }
    fun <ENTITY: LongEntity>getEntityDAO():ENTITY?{
        try {
            @Suppress("UNCHECKED_CAST")
            return (_entityDAO as ENTITY?)
        }catch (ex:Exception){
            println(ex.message)
            return null
        }
    }

    private var propertyBinder: PropertyBinder? = null

    val childDTOs = mutableListOf<CommonDTO>()

    override fun <ENTITY:LongEntity>initialize(
        binder : PropertyBinder,
        dtoModel : DTOClass<ENTITY>
    ){
        propertyBinder = binder
        _dtoModel = dtoModel
        initStatus = InitStatus.PARTIAL
    }

    public override fun clone(): DataModel = this.clone()

    fun toDataModel(): DataModel =  this.injectedDataModel

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
        setId(entity.id.value)
        if(propertyBinder!= null){
            propertyBinder!!.updateProperties(injectedDataModel, entity, UpdateMode.ENTITY_TO_MODEL)
        }else{
            //Issue Warning
        }
    }

//    companion object{
//        val dataSources =  mutableListOf<DataSource<out DataModel>>()
//
//        fun initCommonDTO(){
//
//        }
//
//        fun onDataModelSet(dataModel:DataModel){
//        }
//        inline fun <T: CommonDTO,  DATA:DataModel> T.newDataSource(dataSource : DataSource<DATA>,  sourceItems: List<DataModel>){
//            dataSources.add(dataSource)
//            dataSource.setSourceItems(sourceItems)
//        }
//    }

}
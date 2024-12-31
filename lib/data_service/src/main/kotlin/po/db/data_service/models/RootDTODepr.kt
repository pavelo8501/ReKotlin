package po.db.data_service.models

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.binder.BindingKey
import po.db.data_service.binder.PropertyBinder
import po.db.data_service.binder.UpdateMode
import po.db.data_service.common.enums.InitStatus
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.components.DTORepo
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.OperationsException


//interface DataModelContainerInterface {
//    val isNew: Boolean
//    val dataModel: DataModel
//    fun toDataModel(): DataModel
//    fun getId():Long
//    fun setId(value:Long)
//}

//interface DaoContainerInterface<ENT : LongEntity> {
//    fun addError(msg: String)
//    fun getEntityDAO(): ENT?
//    fun updateDTO(daoEntity: ENT, updateMode: UpdateMode)
//}

//abstract class BaseDTO<DTO>(
//    override  var injectedDataModel: DataModel
//): RootDTO<DataModel, DTO>(injectedDataModel), DTOEntityExtended<DTO>
//        where  DTO : LongEntity {
//
//    override lateinit var  daoEntity:  DTO
//
//}



class DataModelContainer<DATA>(val dataModel: DATA): Cloneable  where  DATA : DataModel{

     fun getId():Long{
        return dataModel.id
    }
    fun setId(value:Long){
        dataModel.id = value
    }

     val isNew : Boolean
        get(){
            return getId() == 0L
        }

    init {
        val a = dataModel
    }

    public override fun clone(): DATA = this.clone()
    fun toDataModel(): DATA =  this.dataModel
}

class DaoContainer<ENT>() where  ENT : LongEntity{
        var initStatus: InitStatus = InitStatus.UNINITIALIZED
            set(value){
                if (value!=field){
                    field = value
                    println(field.msg)
                }
            }

        val errors = mutableListOf<String>()

       lateinit var dataModel: DataModel

         fun addError(msg:String){
            errors.add(msg)
            initStatus = InitStatus.INIT_FAILED
        }

        fun getId(): Long{
            return dataModel.id
        }
        fun setId(value:Long){
            dataModel.id = value
        }

        private lateinit var  daoEntity : ENT

        private var _dtoModel : DTOClass<ENT>? = null
        val dtoModel : DTOClass<ENT>
        get(){return  _dtoModel?:
        throw OperationsException("Trying to access dtoModel property of RootDTOV2 id :${getId()} while undefined",
            ExceptionCodes.LAZY_NOT_INITIALIZED) }

        private val repos = mutableMapOf<BindingKey,DTORepo<ENT>>()
        fun addRepository(key: BindingKey, repo: DTORepo<ENT>){
            repos[key] = repo
        }

        fun <DATA : DataModel,CHILD_ENT: LongEntity>addChildDTO(dto: RootDTODepr<DATA,CHILD_ENT>, repoKey:BindingKey){
           // repos[repoKey]?.add(dto)
        }

        fun <DATA : DataModel,CHILD_ENT: LongEntity>getChildDTOList(repoKey: BindingKey):List<RootDTODepr<DATA, CHILD_ENT>>{
            return emptyList()
           // return repos[repoKey]?.getAll()?: throw OperationsException("Trying to address repository doesn't exist", ExceptionCodes.DTO_SELECTION_FAILURE)
        }

        private var _entityDAO : ENT? = null
        set(value){
            if(value!= null){
                field = value
            }
        }
         fun getEntityDAO():ENT?{
            try {
                return _entityDAO
            }catch (ex:Exception){
                println(ex.message)
                return null
            }
        }

        var propertyBinder: PropertyBinder? = null

        fun initialize(binder : PropertyBinder, dtoModel : DTOClass<ENT>, daoModel: LongEntityClass<ENT>){
            propertyBinder = binder
            _dtoModel = dtoModel
            _entityDAO = _entityDAO
            initStatus = InitStatus.PARTIAL_DTO
        }

         fun updateDTO(daoEntity: ENT, updateMode: UpdateMode){
            propertyBinder?.let {
                _entityDAO = daoEntity
                it.updateProperties(dataModel, daoEntity, updateMode)
                setId(daoEntity.id.value)
                initStatus = InitStatus.PARTIAL_DTO
            }?:run {
                initStatus = InitStatus.INIT_FAILED
            }
        }
}

sealed class RootDTODepr<DATA: DataModel, ENT : LongEntity>(dataModel: DataModel){
    val dataModelContainer = DataModelContainer(dataModel)
    val daoEntityContainer  = DaoContainer<ENT>()
}
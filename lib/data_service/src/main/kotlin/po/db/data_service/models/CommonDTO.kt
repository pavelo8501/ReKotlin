package po.db.data_service.models

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.binder.*
import po.db.data_service.common.enums.InitStatus
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.components.DTOConfig
import po.db.data_service.dto.components.DTORepo
import po.db.data_service.dto.components.HostableRepo
import po.db.data_service.dto.interfaces.DTOEntity
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.OperationsException


sealed interface Hostable{
}

sealed interface Basic{
}

abstract class CommonDTO(override val injectedDataModel : DataModel, override val childDataSource: List<DataModel>? = null): DTOEntity, Cloneable {
    val  baseClassName : String = this::class.simpleName?:"Unknown"

    init {

    }

    var initStatus: InitStatus = InitStatus.UNINITIALIZED
        set(value){
            if(value!= field){
                field = value
                println(field.msg)
            }
        }

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

    val isNew : Boolean
        get(){
            if(getId() == 0L){
                return true
            }
            return false
        }

    private var _dtoModel : DTOClass<*>? = null
    val dtoModel : DTOClass<*>
        get(){
            return  _dtoModel?:
        throw OperationsException("Trying to access dtoModel property of id :${getId()} while undefined",
            ExceptionCodes.LAZY_NOT_INITIALIZED) }

    val repos = mutableMapOf<BindingKey, DTORepo<*>>()

    /**
     * add new repository for CommonDTO objects or return existent is already exists
     *
     */
    inline fun <reified ENTITY : LongEntity>addOrReturn(key: BindingKey, repo: DTORepo<ENTITY>):DTORepo<ENTITY>?{
        if(repos.containsKey(key)){
            val existingRepo  =  repos[key]
            existingRepo?.let {
                if (it.hostDTOClass == repo.hostDTOClass) {
                    return it as? DTORepo<ENTITY>
                }
            }
        }

        repos[key] = repo
        return repo
    }

    fun addChildDTO(dto:CommonDTO, repoKey:BindingKey){
        repos[repoKey]?.add(dto)
    }

    fun getChildDTOList(repoKey: BindingKey):List<CommonDTO>{
        return repos[repoKey]?.getAll()?: throw OperationsException("Trying to address repository doesn't exist", ExceptionCodes.DTO_SELECTION_FAILURE)
    }

    fun getRepository(key: BindingKey): DTORepo<*>? {
        return repos[key]
    }

    private var _entityDAO : LongEntity? = null
        set(value){
            if(value!= null){
                field = value
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

    private lateinit var dtoConfig: DTOConfig<*>

    fun <ENTITY:LongEntity>initialize(config: DTOConfig<ENTITY> ){
        propertyBinder = config.propertyBinder
        _dtoModel = config.parent
        dtoConfig = config
        initStatus = InitStatus.PARTIAL_DTO
    }

    public override fun clone(): DataModel = this.clone()

    fun toDataModel(): DataModel =  this.injectedDataModel

    fun updateDTO(daoEntity: LongEntity, updateMode: UpdateMode):LongEntity?{
        propertyBinder?.let {
            _entityDAO = daoEntity
            it.updateProperties(injectedDataModel, daoEntity, updateMode)
            initStatus =  InitStatus.COMPLETE
            if(updateMode != UpdateMode.MODEL_TO_ENTITY){
                setId(daoEntity.id.value)
            }
            return daoEntity
        }
        return null
    }
}

abstract class HostableDTO<ENTITY>(
     injectedDataModel : DataModel,
     childDataSource: List<DataModel>? = null) :  CommonDTO2(injectedDataModel, childDataSource), Hostable where ENTITY : LongEntity{

     abstract  var hostingDtoModel:  DTOClass<ENTITY>
     override  var entityDAO: LongEntity? = null

     val repos = mutableMapOf<BindingKey, HostableRepo<ENTITY>>()

    /**
     * add new repository for CommonDTO objects or return existent is already exists
     *
     */
    fun  addRepo(key: BindingKey, repo: HostableRepo<ENTITY>){
        if(repos.containsKey(key)){
            val existingRepo  =  repos[key]
            existingRepo?.let {
                if (it.hostDTOClass == repo.hostDTOClass) {

                }
            }
        }

        repos[key] = repo
    }

    fun newDaoEntity():ENTITY{
        return hostingDtoModel.daoFactory.insert(this, hostingDtoModel)!!
    }



    fun repository(key: BindingKey): HostableRepo<ENTITY>? {
        if(repos.containsKey(key)){
            return  repos[key] as HostableRepo<ENTITY>
        }
        return null
    }

    override fun <ENTITY:LongEntity>initialize(config: DTOConfig<ENTITY> ){
        propertyBinder = config.propertyBinder
        dtoModel = config.parent
        dtoConfig = config
        initStatus = InitStatus.PARTIAL_DTO
    }

    override fun copyAsChild(thisClassName: String): HostableDTO<ENTITY> {
        return object : HostableDTO<ENTITY>(this.injectedDataModel, this.childDataSource) {
            override var hostingDtoModel: DTOClass<ENTITY> = this@HostableDTO.hostingDtoModel
            override val dataModel: DataModel = this.injectedDataModel
            override val className: String = thisClassName
        }
    }

}

abstract class CommonDTO2(
    injectedDataModel : DataModel,
    childDataSource:List<DataModel>? = null
) : RootDTO(injectedDataModel,childDataSource) , Basic {

    open fun copyAsChild(thisClassName: String): CommonDTO2 {
        return object : CommonDTO2(this.injectedDataModel, this.childDataSource) {
            override val dataModel: DataModel = this.injectedDataModel
            override val className: String = thisClassName
        }
    }

    fun <ENTITY: LongEntity>copyAsHostable(thisClassName: String, companion: DTOClass<ENTITY>): HostableDTO<ENTITY> {
        return object : HostableDTO<ENTITY>(this.injectedDataModel, this.childDataSource) {
            override var hostingDtoModel: DTOClass<ENTITY> = companion
            override val dataModel: DataModel = this.injectedDataModel
            override val className: String = thisClassName
        }
    }

}

sealed class RootDTO(
    override val injectedDataModel : DataModel,
    override val childDataSource: List<DataModel>? = null): DTOEntity, Cloneable {
        val  baseClassName : String = this::class.simpleName?:"Unknown"

        var initStatus: InitStatus = InitStatus.UNINITIALIZED
        set(value){
            if(value!= field){
                field = value
                println(field.msg)
            }
        }

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

        val isNew : Boolean
        get(){
            if(getId() == 0L){
                return true
            }
            return false
        }

        var dtoModel : DTOClass<*>? = null
            get(){
            return  field?:
            throw OperationsException("Trying to access dtoModel property of id :${getId()} while undefined",
                ExceptionCodes.LAZY_NOT_INITIALIZED) }

        open var entityDAO : LongEntity? = null

        var propertyBinder: PropertyBinder? = null

        lateinit var dtoConfig: DTOConfig<*>

        open fun <T: LongEntity> initialize(config: DTOConfig<T> ){
            propertyBinder = config.propertyBinder
            dtoModel = config.parent
            initStatus = InitStatus.PARTIAL_DTO
        }

        public override fun clone(): DataModel = this.clone()

        fun toDataModel(): DataModel =  this.injectedDataModel

        fun updateDTO(daoEntity: LongEntity, updateMode: UpdateMode):LongEntity?{
            propertyBinder?.let {
                entityDAO = daoEntity
                it.updateProperties(injectedDataModel, daoEntity, updateMode)
                initStatus =  InitStatus.COMPLETE
                if(updateMode != UpdateMode.MODEL_TO_ENTITY){
                    setId(daoEntity.id.value)
                }
                return daoEntity
            }
            return null
        }

}




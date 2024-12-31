package po.db.data_service.dto

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Database
import po.db.data_service.binder.BindingKey
import po.db.data_service.binder.OrdinanceType
import po.db.data_service.binder.RelationshipBinder
import po.db.data_service.common.enums.InitStatus
import po.db.data_service.dto.components.DTOConfig
import po.db.data_service.dto.components.DTOFactory
import po.db.data_service.dto.components.DTORepo
import po.db.data_service.dto.components.HostableRepo
import po.db.data_service.dto.interfaces.DTOEntity
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.OperationsException
import po.db.data_service.models.Basic
import po.db.data_service.models.CommonDTO
import po.db.data_service.models.CommonDTO2
import po.db.data_service.models.Hostable
import po.db.data_service.models.HostableDTO
import po.db.data_service.models.RootDTO
import po.db.data_service.scope.service.ServiceContext
import po.db.data_service.scope.service.models.DaoFactory
import kotlin.reflect.KClass


abstract class DTOClass<ENTITY>(val entityClass : KClass<ENTITY>) where ENTITY : LongEntity  {

    lateinit var dbConnection : Database
    var initStatus:InitStatus = InitStatus.UNINITIALIZED
    var className : String = "Undefined"

    lateinit var conf : DTOConfig<ENTITY>
    lateinit var dtoFactory :  DTOFactory<ENTITY>
    val repository = HostableRepo<ENTITY>(this, BindingKey(OrdinanceType.ONE_TO_MANY,"container"),0)

    val daoModel: LongEntityClass<ENTITY>
        get(){
            return  conf.daoModel?: throw OperationsException("Unable read daoModel property on $className", ExceptionCodes.LAZY_NOT_INITIALIZED)
        }

    lateinit var daoFactory : DaoFactory

    protected abstract fun modelSetup()

    fun nowTime(): LocalDateTime {
        return LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
    }

    fun <SERVICE_ENTITY: LongEntity>  toDtoContext(serviceContext: ServiceContext<SERVICE_ENTITY>, body: DTOContext<SERVICE_ENTITY, ENTITY>.()->Unit ){
        DTOContext<SERVICE_ENTITY, ENTITY>(this, daoFactory, dtoFactory).body()
    }

    fun <ENTITY:LongEntity>  ServiceContext<ENTITY>.service(): ServiceContext<ENTITY>{
        return this
    }

    fun getAssociatedTables():List<IdTable<Long>>{
       val result = mutableListOf<IdTable<Long>>()
       result.add(this.daoModel.table)
       result.addAll(conf.relationBinder.getDependantTables())
       return result
    }

    private fun selfConfig(){
        conf = DTOConfig(this)
        daoFactory = DaoFactory(dbConnection)
        dtoFactory = DTOFactory()
        //"DtoFactory|${parent.className}"
    }

    fun initialization(connection: Database,  body : (DTOClass<ENTITY>.()->Unit)? = null){
        try {
            dbConnection = connection
            selfConfig()
            body?.invoke(this)
            modelSetup()
            initStatus = InitStatus.COMPLETE
        }catch (ex:Exception){
            println(ex.message)
            initStatus = InitStatus.INIT_FAILED
        }
    }

    private fun getFromRepo(id:Long): HostableDTO<ENTITY>?{
       return repository.get(id)
    }

    fun createRepository(key : BindingKey, parentId: Long ): HostableRepo<ENTITY>{
        try {
            return HostableRepo<ENTITY>(this,key,parentId)
        }catch (ex:Exception){
            println(ex.message)
            throw ex
        }
    }

    fun addDtoToRepository(dto: HostableDTO<ENTITY>, repo  :  HostableRepo<ENTITY>){
         repo.add(dto)
         val a =10
    }



    fun copyAsCommonDTO(dto: HostableDTO<*>):CommonDTO2{
        return when (dto) {
            is Hostable -> dto.copyAsChild(dto.className)
        }
    }

    fun copyAsHostableDTO(dto: CommonDTO2): HostableDTO<ENTITY>{
        return when (dto) {
            is Basic -> dto.copyAsHostable(dto.className, this)
        }
    }



    /**
     * Reinitialize DTO from  and check consistency
     * if id is 0 initialize as new else lookup in the repository for existent copy
     * if existent copy differs from the dto supplied substitute it with dto supplied
     * @input dtoList: List<CommonDTO>
     * @return List<CommonDTO>
     * */
    fun reInitFromModel(dtoModel: DTOClass<*>):List<CommonDTO2>{
        val dtoList = dtoModel.repository.getAll().map { copyAsCommonDTO(it) }
        return reInit(dtoList).getAll()
    }


    /**
     * Reinitialize DTO and check consistency
     * if id is 0 initialize as new else lookup in the repository for existent copy
     * if existent copy differs from the dto supplied substitute it with dto supplied
     * @input dtoList: List<CommonDTO>
     * @return List<CommonDTO>
     * */
    fun reInit(dtoList: List<CommonDTO2>): HostableRepo<ENTITY>{
        val resultingList = mutableListOf<CommonDTO>()
        val repo =  this.repository
        dtoList.filter { it.getId() != 0L }.let { }

        dtoList.filter { it.getId() == 0L }.map { it.toDataModel() }.forEach {dataModel->

             create(dataModel, repo)
        }
        return repo
    }

    /**
     * Create new CommonDTO entity from DataModel provided
     * @input dataModel: DataModel
     * @return CommonDTO or null
     * */
    fun create(dataModel: DataModel, repository:  HostableRepo<ENTITY>) {
        repository.get(dataModel.id).let { dto ->
            when (dto) {
                null -> {
                    dtoFactory.constructDtoEntity(dataModel)?.let { commonDto ->
                        val hostableDTO = this.copyAsHostableDTO(commonDto)
                        hostableDTO.initialize(conf)
                        repository.add(hostableDTO)
                        conf.relationBinder.bindings().forEach { container ->
                            container.createChildEntities(hostableDTO)
                            repository.add(hostableDTO)
                        }
                    }
                }
                else -> {

                }
            }
        }
    }

    /**
     * Create new CommonDTO entity from DAO LongEntity
     * @input daoEntity: LongEntity
     * @return CommonDTO or null
     * */
    fun create(daoEntity: ENTITY) : HostableDTO<ENTITY>? {
        val dto = repository.get(daoEntity.id.value)?: run {
            dtoFactory.constructDtoEntity()
        }
        if(dto != null){
            val hostableDTO = this.copyAsHostableDTO(dto)
            conf.propertyBinder?.let {
                hostableDTO.initialize(conf)
                hostableDTO.let {
                    conf.relationBinder.bindings().forEach { bindContainer ->

                        // bindContainer.createOrGetRepository(dto)
                        bindContainer.loadChild(hostableDTO, daoFactory)
                    }
                }
                repository.add(hostableDTO)
            }
            return hostableDTO
        }else{
            return null
        }
    }

    fun setHostEntityClass(entity:KClass<ENTITY>){
        //@Suppress("UNCHECKED_CAST")
        //this.entityClass = entity::class
    }


    inline fun <reified DTO, reified DATA>  dtoSettings(
        daoModel: LongEntityClass<ENTITY>,
        block: DTOConfig<ENTITY>.() -> Unit
    ) where  DTO : DTOEntity, DATA : DataModel{
        val rootDtoModelClass = DTO::class
        this.className  = rootDtoModelClass.simpleName!!
        conf.also{
            it.setClassData<DTO>(rootDtoModelClass, DATA::class, daoModel)
            it.block()
        }
    }

    inline fun <DTO> relationBindings(
        context: RelationshipBinder<ENTITY>.()-> Unit
    )where DTO : DTOEntity{
        conf.relationBinder.context()
    }

}


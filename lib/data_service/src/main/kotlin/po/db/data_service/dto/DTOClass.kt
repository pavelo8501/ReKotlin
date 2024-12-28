package po.db.data_service.dto

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Database
import po.db.data_service.binder.RelationshipBinder
import po.db.data_service.common.enums.InitStatus
import po.db.data_service.constructors.ConstructorBuilder
import po.db.data_service.dto.components.DTOConfig
import po.db.data_service.dto.components.DTOFactory
import po.db.data_service.dto.components.DTORepo
import po.db.data_service.dto.interfaces.DTOEntity
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.OperationsException
import po.db.data_service.models.CommonDTO
import po.db.data_service.scope.service.ServiceContext
import po.db.data_service.scope.service.models.DaoFactory


inline fun <ENTITY : LongEntity> DTOClass<ENTITY>.startInitSequence(

    body : ServiceContext<ENTITY>.()->Unit
){

}

abstract class DTOClass<ENTITY> where ENTITY : LongEntity  {

    lateinit var dbConnection : Database
    var initStatus:InitStatus = InitStatus.UNINITIALIZED
    var initialized: Boolean = false
    var className : String = "Undefined"

    lateinit var conf : DTOConfig<ENTITY>
    lateinit var dtoFactory :  DTOFactory<ENTITY>


    val daoModel: LongEntityClass<ENTITY>
        get(){
            return  conf.daoModel?: throw OperationsException("Unable read daoModel property on $className", ExceptionCodes.LAZY_NOT_INITIALIZED)
        }

    lateinit var daoFactory : DaoFactory

    val dtoContainer = mutableListOf<CommonDTO>()

    protected abstract fun modelSetup()

    fun nowTime(): LocalDateTime {
        return LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
    }

    fun <SERVICE_ENTITY: LongEntity>getDtoContext(serviceContext: ServiceContext<SERVICE_ENTITY>):DTOContext<SERVICE_ENTITY, ENTITY>{
       return DTOContext(this)
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

    private fun getFromRepo(id:Long):CommonDTO?{
        return null
    }

    /**
     * Create new CommonDTO entity from DataModel provided
     * @input dataModel: DataModel
     * @return CommonDTO
     * */
    fun create(dataModel: DataModel) : CommonDTO? {
       val dto = getFromRepo(dataModel.id)?: run {
           dtoFactory.constructDtoEntity(dataModel)
        }
        if(dto!=null){
            conf.propertyBinder?.let {propBinder->
                dto.initialize(propBinder,this)
                dtoContainer.add(dto)
                conf.relationBinder.getBindingList().forEach {
                    it.addDTORepository(dto)

                }
                return dto
            }
            return null
        }else{
            return null
        }

       // val newDTO = dtoFactory.constructDtoEntity(dataModel)
//        if(dto!=null){
//            dtoContainer.add(newDTO)
//            conf.relationBinder.lastKeys
////            conf.relationBinder.getBindingList().forEach { binding->
////                if(newDTO.childDataSource!= null){
////                    newDTO.childDataSource.forEach { dataModel->
////                        val childDTO = binding.createChild(newDTO, dataModel)
////                        newDTO.childDTOs.add(childDTO)
////                    }
////                }
////            }
//            return  newDTO
//        }else{
//            return null
//        }
    }

    fun create(daoEntity: ENTITY) : CommonDTO? {
        val newDTO = dtoFactory.constructDtoEntity()
        if(newDTO!=null){
            newDTO.updateDTO(daoEntity, this)
//            conf.relationBinder.getBindingList().forEach { binding ->
//                binding.loadChild( newDTO, dtoFactory!!) .let {
//                    newDTO.childDTOs.addAll(it)
//                }
//            }
            dtoContainer.add(newDTO)
            return newDTO
        }else{
            return null
        }
    }

//    fun initializeDTOEntities(dataModels: List<DataModel>){
//        dataModels.forEach {dataModel->
//
//        }
//    }

    inline fun <reified DTO, reified DATA>  dtoSettings(
        daoModel: LongEntityClass<ENTITY>,
        block: DTOConfig<ENTITY>.() -> Unit
    ) where  DTO : DTOEntity, DATA : DataModel {
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


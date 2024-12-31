package po.db.data_service.scope.service

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.DTOContext
import po.db.data_service.dto.components.DTORepo
import po.db.data_service.dto.components.HostableRepo
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.dto.models.DTOResult
import po.db.data_service.models.CommonDTO
import po.db.data_service.models.CommonDTO2
import po.db.data_service.models.HostableDTO
import po.db.data_service.scope.service.interfaces.ServiceInterface
import po.db.data_service.scope.service.models.DaoFactory
import po.db.data_service.scope.service.models.SequenceTag

class ServiceContext<ENTITY>(
    private val dbConnection: Database,
    private val rootDtoModel : DTOClass<ENTITY>,
): ServiceInterface<ENTITY> where  ENTITY : LongEntity{
    val name : String = rootDtoModel.className + "|Service"
    val sequenceRegistry = mutableMapOf<SequenceTag, DTOContext<*, ENTITY>.(DTOClass<*>)->Unit>()
    val daoFactory = DaoFactory(dbConnection)

    private fun  <T>dbQuery(body : () -> T): T = transaction(dbConnection) {
        body()
    }
    private fun <T> serviceContext( statement: ServiceContext<ENTITY>.() -> T): T = statement.invoke(this)
    fun <T> context(serviceBody: ServiceContext<ENTITY>.() -> T): T = serviceContext{
        serviceBody()
    }


    fun DTOClass<ENTITY>.select(block: DTOContext<ENTITY, ENTITY>.() -> Unit) {
       // val dtoEntities =   daoFactory.select(this).map { create(it) }
       // val context = this.getDtoContext(this@ServiceContext)

    }

    fun DTOClass<ENTITY>.update(dtoList : List<CommonDTO2>, block: DTOContext<ENTITY,ENTITY>.() -> Unit): Unit {
            this.reInit(dtoList).let {it.getAll().filter{ it.isNew}.let{new->
                toDtoContext(this@ServiceContext){this.update(new) {  } }
            }
        }
    }

    @JvmName("updateListDataModel")
    fun DTOClass<ENTITY>.update(dataModelList : List<DataModel>, block: DTOClass<ENTITY>.() -> Unit): Unit {
       val result = dataModelList.map { this.create(it, this.repository)}
    }

    fun <DTO, DTO_ENTITY> DTOClass<DTO_ENTITY>.dtoSequence(tag:SequenceTag, context: DTOContext<ENTITY, DTO_ENTITY>.(DTOClass<DTO_ENTITY>)->Unit):DTOResult<DTO_ENTITY> where DTO : DTOClass<DTO_ENTITY>, DTO_ENTITY : LongEntity{
        return DTOResult(this)
    }




}
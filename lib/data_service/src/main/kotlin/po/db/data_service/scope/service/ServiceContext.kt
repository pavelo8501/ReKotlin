package po.db.data_service.scope.service

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import po.db.data_service.dto.DTOClass
import po.db.data_service.scope.dto.DTOContext
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.EntityDTO
import po.db.data_service.scope.service.models.DaoFactory

class ServiceContext<DATA,ENTITY>(
    private val dbConnection: Database,
    private val rootDtoModel : DTOClass<DATA,ENTITY>,
) where  ENTITY : LongEntity,DATA: DataModel{

    val name : String = rootDtoModel.className + "|Service"

    private val daoFactory = DaoFactory(dbConnection)

    private fun  <T>dbQuery(body : () -> T): T = transaction(dbConnection) {
        body()
    }

    private fun <T> service(statement: ServiceContext<DATA, ENTITY>.() -> T): T = statement.invoke(this)
    fun <T> context(serviceBody: ServiceContext<DATA, ENTITY>.() -> T): T = service{
        serviceBody()
    }

    fun DTOClass<DATA, ENTITY>.select(block: DTOContext<DATA, ENTITY>.() -> Unit){
        val selectedDTOs = dbQuery {
           select()
        }
        val context  = DTOContext(selectedDTOs)
        context.block()
    }

    @JvmName("updateDataModels")
    fun DTOClass<DATA, ENTITY>.update(
        dataModels : List<DATA>,
        block: DTOClass<DATA,ENTITY>.() -> Unit){
        dbQuery{
            dataModels.map{create<DATA,ENTITY>(it)}
        }
        this.block()
    }

    fun DTOClass<DATA, ENTITY>.update(
        dataModes : List<EntityDTO<DATA, ENTITY>>,
        block: DTOClass<DATA, ENTITY>.() -> Unit
    ){
        dataModes.forEach {
            initDTO(it)
        }
        this.block()
    }

    fun DTOClass<DATA, ENTITY>.sequence(name:String):DTOClass<DATA, ENTITY>{
        return this
    }
}
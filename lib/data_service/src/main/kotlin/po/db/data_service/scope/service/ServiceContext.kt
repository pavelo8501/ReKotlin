package po.db.data_service.scope.service

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import po.db.data_service.dto.DTOClass
import po.db.data_service.scope.dto.DTOContext
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.EntityDTO
import po.db.data_service.scope.service.enums.WriteMode

class ServiceContext<DATA,ENTITY>(
    private val dbConnection: Database,
    private val rootDtoModel : DTOClass<DATA,ENTITY>,
) where  ENTITY : LongEntity,DATA: DataModel{

    val name : String = rootDtoModel.className + "|Service"

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

    @JvmName("updateFromDataModels")
    fun DTOClass<DATA, ENTITY>.update(
        dataModels : List<DATA>,
        writeMode: WriteMode = WriteMode.STRICT,
        block: DTOContext<DATA, ENTITY>.() -> Unit){
        val createdDTOs =  dbQuery {
            create<DATA, ENTITY>(dataModels)
        }
        val context = DTOContext(createdDTOs)
        context.block()
    }

    fun DTOClass<DATA, ENTITY>.update(
        dataModels : List<EntityDTO<DATA, ENTITY>>,
        block: DTOClass<DATA, ENTITY>.() -> Unit
    ){
        TODO("To implement update variance if EntityDTO list is supplied")
    }

    fun DTOClass<DATA, ENTITY>.sequence(name:String):DTOClass<DATA, ENTITY>{
        return this
    }
}
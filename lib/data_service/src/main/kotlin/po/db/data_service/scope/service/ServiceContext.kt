package po.db.data_service.scope.service

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.CommonDTO
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

    private fun <T> serviceContext( statement: ServiceContext<DATA,ENTITY>.() -> T): T = statement.invoke(this)
    fun <T> context(serviceBody: ServiceContext<DATA,ENTITY>.() -> T): T = serviceContext{
        serviceBody()
    }

    fun DTOClass<DATA,ENTITY>.select(block: DTOClass<DATA,ENTITY>.() -> Unit): Unit {
        daoFactory.all(this).forEach {
            dbQuery {
                //this.create(it)
            }
        }
        this.block()
    }

    @JvmName("updateDataModels")
    fun DTOClass<DATA,ENTITY>.update(dataModels : List<DATA> , block: DTOClass<DATA,ENTITY>.() -> Unit): Unit {
        dbQuery{
            dataModels.forEach {
               val result =  create(it)
               val a =10
            }
        }
        this.block()
    }

    fun DTOClass<DATA,ENTITY>.update(dataModelList : List<EntityDTO<DATA,ENTITY> >, block: DTOClass<DATA,ENTITY>.() -> Unit): Unit {
        dataModelList.forEach {
            initDTO(it)
        }
        this.block()
    }

    fun DTOClass<DATA,ENTITY>.sequence(name:String):DTOClass<DATA,ENTITY>{
        return this
    }
}
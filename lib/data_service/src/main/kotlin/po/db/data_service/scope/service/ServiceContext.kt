package po.db.data_service.scope.service

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.scope.service.models.DaoFactory

class ServiceContext<ENTITY>(
    private val dbConnection: Database,
    private val rootDtoModel : DTOClass<ENTITY>,
) where  ENTITY : LongEntity{

    val name : String = rootDtoModel.className + "|Service"

    private val daoFactory = DaoFactory(dbConnection)

    private fun  <T>dbQuery(body : () -> T): T = transaction(dbConnection) {
        body()
    }

    private fun <T> serviceContext( statement: ServiceContext<ENTITY>.() -> T): T = statement.invoke(this)
    fun <T> context(serviceBody: ServiceContext<ENTITY>.() -> T): T = serviceContext{
        serviceBody()
    }

    fun DTOClass<ENTITY>.select(block: DTOClass<ENTITY>.() -> Unit): Unit {
        daoFactory.all(this).forEach {
            dbQuery {
                this.create(it)
            }
        }
        this.block()
    }

    fun DTOClass<ENTITY>.update(dataModel : DataModel , block: DTOClass<ENTITY>.() -> Unit): Unit {
        val result =  this.create(dataModel, daoFactory)
        this.block()
    }

    fun DTOClass<ENTITY>.update(dataModelList : List<DataModel>, block: DTOClass<ENTITY>.() -> Unit): Unit {
        dataModelList.forEach { dataModel ->
            this.create(dataModel, daoFactory)
        }
        this.block()
    }

//    fun DTOClass<ENTITY>.sequence(name:String, block: DTOClass<ENTITY>.() -> Unit){
//
//    }

    fun DTOClass<ENTITY>.sequence(name:String):DTOClass<ENTITY>{
        return this
    }


}
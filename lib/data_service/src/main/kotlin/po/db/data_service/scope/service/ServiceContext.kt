package po.db.data_service.scope.service

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DataModel
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
                this.create(it)
            }
        }
        this.block()
    }

    fun DTOClass<DATA,ENTITY>.update(dataModel : DATA , block: DTOClass<DATA,ENTITY>.() -> Unit): Unit {
        val result =  this.create(dataModel, daoFactory)
        this.block()
    }

    fun DTOClass<DATA,ENTITY>.update(dataModelList : List<DATA>, block: DTOClass<DATA,ENTITY>.() -> Unit): Unit {
        dataModelList.forEach { dataModel ->
            this.create(dataModel, daoFactory)
        }
        this.block()
    }

//    fun DTOClass<ENTITY>.sequence(name:String, block: DTOClass<ENTITY>.() -> Unit){
//
//    }

    fun DTOClass<DATA,ENTITY>.sequence(name:String):DTOClass<DATA,ENTITY>{
        return this
    }


}
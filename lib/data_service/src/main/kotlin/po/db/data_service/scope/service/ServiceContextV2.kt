package po.db.data_service.scope.service

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.transactions.transaction
import po.db.data_service.dto.DTOClass
import po.db.data_service.models.CommonDTO

class ServiceContextV2<ENTITY>(
    private val dbConnection: Database,
    private val rootDtoModel : DTOClass<ENTITY>,
) where  ENTITY : LongEntity{

    val name : String = rootDtoModel.className + "|Service"

    private fun  <T>dbQuery(body : () -> T): T = transaction(dbConnection) {
        body()
    }

    private fun <T> serviceContext( statement: ServiceContextV2<ENTITY>.() -> T): T = statement.invoke(this)
    fun <T> context(serviceBody: ServiceContextV2<ENTITY>.() -> T): T = serviceContext{
        serviceBody()
    }

    private fun <E : LongEntity>daoSelect(entityModel : LongEntityClass<E>): SizedIterable<E>{
       val result =  dbQuery {
            entityModel.all()
        }
        return result
    }

    fun DTOClass<ENTITY>.select(block: DTOClass<ENTITY>.(List<CommonDTO>) -> Unit): Unit {
        val result  = mutableListOf<CommonDTO>()
        dbQuery {
            daoSelect(this.daoModel).forEach {
                val dtoEntity =  this.create(it)
                result.add(dtoEntity)
            }
        }
      //  block.invoke(result.toList())
        //  return result
    }

    fun DTOClass<ENTITY>.update(list : List<CommonDTO>, block: DTOClass<ENTITY>.() -> Unit): Unit {
       // list.forEach { this@ServiceContext.initDTO(it) }
        this.block()
    }

}
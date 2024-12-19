package po.db.data_service.scope.service

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.transactions.transaction
import po.db.data_service.binder.DTOPropertyBinder
import po.db.data_service.dto.DTOClassV2
import po.db.data_service.models.CommonDTOV2


class ServiceContextV2(
    private val dbConnection: Database,
    private val rootDtoModel : DTOClassV2,
){

    val name : String = rootDtoModel.className + "|Service"

    private fun  <T>dbQuery(body : () -> T): T = transaction(dbConnection) {
        body()
    }

    private fun <T> serviceContext( statement: ServiceContextV2.() -> T): T = statement.invoke(this)
    fun <T> context(serviceBody: ServiceContextV2.() -> T): T = serviceContext{
        serviceBody()
    }

    private fun select(entityModel : LongEntityClass<LongEntity>): SizedIterable<LongEntity>{
       val result =  dbQuery {
            entityModel.all()
        }
        return result
    }

    fun  DTOClassV2.select(block: DTOClassV2.(List<CommonDTOV2>) -> Unit): Unit {
        val result  = mutableListOf<CommonDTOV2>()
        dbQuery {
            select(this.daoModel).forEach {
                val dtoEntity =  this.create(serviceContext, it)
                result.add(dtoEntity)
            }
        }


      //  block.invoke(result.toList())
        //  return result
    }



}
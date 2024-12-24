package po.db.data_service.scope.service

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.transactions.transaction
import po.db.data_service.binder.DTOPropertyBinder
import po.db.data_service.dto.DTOClass
import po.db.data_service.models.CommonDTO


class ServiceContextV2(
    private val dbConnection: Database,
    private val rootDtoModel : DTOClass,
){

    val name : String = rootDtoModel.className + "|Service"

    private fun  <T>dbQuery(body : () -> T): T = transaction(dbConnection) {
        body()
    }

    private fun <T> serviceContext( statement: ServiceContextV2.() -> T): T = statement.invoke(this)
    fun <T> context(serviceBody: ServiceContextV2.() -> T): T = serviceContext{
        serviceBody()
    }

    private fun daoSelect(entityModel : LongEntityClass<LongEntity>): SizedIterable<LongEntity>{
       val result =  dbQuery {
            entityModel.all()
        }
        return result
    }

    fun  DTOClass.select(block: DTOClass.(List<CommonDTO>) -> Unit): Unit {
        val result  = mutableListOf<CommonDTO>()
        dbQuery {
            daoSelect(this.daoModel).forEach {
                val dtoEntity =  this.create(it){
                    val a = 10
                }
                result.add(dtoEntity)
            }
        }


      //  block.invoke(result.toList())
        //  return result
    }



}
package po.db.data_service.scope.service

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
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

    fun  DTOClassV2.select(block: DTOClassV2.(List<CommonDTOV2>) -> Unit): Unit {
        val result  = mutableListOf<CommonDTOV2>()
        dbQuery {
            this.daoModel.all().forEach {
                result.add(this.create(it))
            }
        }
      //  block.invoke(result.toList())
        //  return result
    }



}
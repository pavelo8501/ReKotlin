package po.db.data_service.scope.service.models

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import po.db.data_service.dto.DTOClass
import po.db.data_service.models.CommonDTO

class DaoFactory(private val connection : Database) {

    private fun  <T>dbQuery(body : () -> T): T = transaction(connection) {
        body()
    }

    fun <ENTITY: LongEntity>update(dtoModel: DTOClass<ENTITY>, dtoEntity: CommonDTO ){

        if(dtoEntity.id == 0L){
            dtoModel.daoModel.new {
                dtoEntity.newDAO(this)
            }
        }else{

        }
    }
}
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

    fun <ENTITY: LongEntity>new(dtoModel : DTOClass<ENTITY>, fn: ((ENTITY)->Unit)? = null ): ENTITY{
        val daoEntity = dbQuery {
            dtoModel.daoModel.new {
                fn?.invoke(this)
            }
        }
        return daoEntity
    }

    fun <ENTITY: LongEntity>all(dtoModel: DTOClass<ENTITY>): List<ENTITY> {
        val result = dbQuery {
            return@dbQuery  dtoModel.daoModel.all().toList()
        }
        return result
    }

    fun <ENTITY: LongEntity>update(dtoEntity: CommonDTO, dtoModel : DTOClass<ENTITY>): LongEntity?{
      val daoEntity =  if(dtoEntity.id == 0L){
          dbQuery {
              dtoModel.daoModel.new {
                  dtoEntity.updateDAO(this)
              }
          }
        }else{
            null
      }
      if(daoEntity != null){
          dtoEntity.id = daoEntity.id.value
      }
      return daoEntity
    }
}
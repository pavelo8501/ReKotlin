package po.db.data_service.scope.service.models

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import po.db.data_service.dto.DTOClass
import po.db.data_service.models.CommonDTO

class DaoFactory(private val connection : Database) {

    fun  <T>dbQuery(body : () -> T): T = transaction(connection) {
        body()
    }

    fun <ENTITY: LongEntity>new(commonDTO: CommonDTO, dtoModel : DTOClass<ENTITY>): ENTITY?{
        try {
            val daoEntity = dbQuery {
                dtoModel.daoModel.new {
                    commonDTO.updateDAO(this)
                }
            }
            return daoEntity
        }catch (ex:Exception){
            return null
        }
    }

    fun <ENTITY: LongEntity>all(dtoModel: DTOClass<ENTITY>): List<ENTITY> {
        val result = dbQuery {
            return@dbQuery  dtoModel.daoModel.all().toList()
        }
        return result
    }

    fun <ENTITY: LongEntity>update(dtoEntity: CommonDTO, dtoModel : DTOClass<ENTITY>): LongEntity?{
      try {
          val daoEntity = dbQuery {
              dtoEntity.getEntityDAO<ENTITY>().let {
                  if (it != null) {
                      dtoEntity.updateDAO(it)
                      return@dbQuery it
                  } else {
                      dtoModel.daoModel.get(dtoEntity.dataModel.id).let { newDao ->
                          dtoEntity.updateDTO(newDao, dtoModel)
                          return@dbQuery newDao
                      }
                  }
              }
          }
          return daoEntity
      }catch (ex:Exception){
          println(ex.message)
          return null
      }
    }
}
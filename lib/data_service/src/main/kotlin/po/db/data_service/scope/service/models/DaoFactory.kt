package po.db.data_service.scope.service.models

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import po.db.data_service.binder.UpdateMode
import po.db.data_service.dto.DTOClass
import po.db.data_service.models.CommonDTO
import po.db.data_service.models.HostableDTO

class DaoFactory(private val connection : Database) {

    fun  <T>dbQuery(body : () -> T): T = transaction(connection) {
        body()
    }

    fun <ENTITY: LongEntity>insert(commonDTO: HostableDTO<ENTITY>, dtoModel: DTOClass<ENTITY>, body: (DTOClass<ENTITY>.()->Unit)? = null ):ENTITY?{
        try {
            val daoEntity =  dtoModel.daoModel.new {

            }
            return daoEntity
        }catch (ex:Exception){
            println(ex.message)
            return null
        }
    }

    fun <ENTITY: LongEntity>select(dtoModel: DTOClass<ENTITY>): List<ENTITY> {
        val result = dbQuery {
            return@dbQuery  dtoModel.daoModel.all().toList()
        }
        return result
    }

    fun <ENTITY: LongEntity>update(dtoEntity: HostableDTO<ENTITY>, dtoModel : DTOClass<ENTITY>):Boolean{
      try {
          val daoEntity = dbQuery {
              dtoEntity.entityDAO.let {
                  if (it != null) {
                      dtoEntity.updateDTO(it, UpdateMode.MODEL_TO_ENTITY)
                      return@dbQuery it
                  } else {
                      dtoModel.daoModel[dtoEntity.dataModel.id].let { newDao ->
                          dtoEntity.updateDTO(newDao, UpdateMode.ENTITY_TO_MODEL)
                          return@dbQuery newDao
                      }
                  }
              }
          }
          return true
      }catch (ex:Exception){
          println(ex.message)
          return false
      }
    }
}
package po.db.data_service.scope.service.models

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import po.db.data_service.binder.UpdateMode
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.EntityDTO

class DaoFactory(private val connection : Database)  {

    private fun  <T>dbQuery(body : () -> T): T = transaction(connection) {
        body()
    }

    fun <ENTITY: LongEntity, DATA: DataModel>new(
        dtoModel : DTOClass<DATA,ENTITY>,
        fn: ((ENTITY)->Unit)? = null
    ): ENTITY{
        val daoEntity = dbQuery {
            dtoModel.entityModel.new {
                fn?.invoke(this)
            }
        }
        return daoEntity
    }

    fun <ENTITY: LongEntity, DATA: DataModel>all(dtoModel: DTOClass<DATA,ENTITY>): List<ENTITY> {
        val result = dbQuery {
            return@dbQuery dtoModel.entityModel.all().toList()
        }
        return result
    }

    fun <DATA: DataModel, ENTITY: LongEntity>update(
        dtoEntity: EntityDTO<DATA, ENTITY>,
        dtoModel : DTOClass<DATA,ENTITY>
    ): LongEntity?{
      val daoEntity =  if(dtoEntity.id == 0L){
          dbQuery {
              dtoModel.entityModel.new {
                  dtoEntity.update(this, UpdateMode.ENTITY_TO_MODEL)
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
package po.db.data_service.transportation

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import po.db.data_service.dto.AbstractDTOModel
import po.db.data_service.dto.DTOMarker
import po.db.data_service.services.models.ServiceUniqueKey
import po.db.data_service.structure.ServiceContext


enum  class TableCreateMode{
    CREATE,
    FORCE_RECREATE
}

data class ServiceCreateOptions<DATA_MODEL, ENTITY>(
    val createTable: TableCreateMode = TableCreateMode.CREATE,
) where DATA_MODEL : DTOMarker, ENTITY : LongEntity
{
    var service: ServiceContext<DATA_MODEL, ENTITY> ? = null
}


class ServiceRouter(
    private val connectionName: String,
    private val connection: Database,
)
//where DATA_MODEL : DTOMarker
{

    //private val services :  MutableMap<String, ServiceContext<*,*>> = mutableMapOf()

    private val serviceRegistry = hashMapOf<ServiceUniqueKey, ServiceContext<*,*>>()

    private fun <DATA_MODEL : DTOMarker, ENTITY : LongEntity> createService(
        serviceName:String,
        dtoModel : AbstractDTOModel<DATA_MODEL>,
        entityModel : ENTITY ) : ServiceContext<DATA_MODEL, ENTITY>{

        return ServiceContext(serviceName,connection,dtoModel,entityModel)
    }

    private fun <DATA_MODEL, ENTITY> getOrCreateService(
        routeKey: ServiceUniqueKey,
        dataModel: AbstractDTOModel<DATA_MODEL>,
        entityModel : ENTITY
    ) : ServiceContext<*, *>  where DATA_MODEL : DTOMarker, ENTITY : LongEntity{

      val service =  serviceRegistry.getOrPut(routeKey) {
          createService<DATA_MODEL,ENTITY>("${connectionName}|${routeKey.sysName}", dataModel, entityModel)
      }

        return service
    }

    fun <DATA_MODEL : DTOMarker, ENTITY: LongEntity >initializeRoute(
        serviceUniqueKey: ServiceUniqueKey,
        dtoModel : AbstractDTOModel<DATA_MODEL>,
        entityModel : ENTITY
    ): ServiceContext<*, *> {

        getOrCreateService(serviceUniqueKey, dtoModel,  entityModel).let {
            return it
        }
    }
}



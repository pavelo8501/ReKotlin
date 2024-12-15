package po.db.data_service.scope.service

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.CommonDTO
import po.db.data_service.scope.connection.controls.ServiceRouter
import po.db.data_service.scope.service.controls.ServiceRegistryBuilder
import po.db.data_service.scope.service.controls.service_registry.ServiceUniqueKey
import po.db.data_service.scope.service.controls.service_registry.serviceRegistry

class ServiceClass<DATA_MODEL, ENTITY>(
    val connection :Database,
    val connectionName : String,
    val rootDataModel : DTOClass<DATA_MODEL, ENTITY>,
) where DATA_MODEL:DataModel,  ENTITY : LongEntity {


  //  val serviceRouter = ServiceRouter(connectionName, connection, serviceRegistry)


}
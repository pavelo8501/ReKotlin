package po.db.data_service.scope.service

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.Database
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.CommonDTO
import po.db.data_service.scope.connection.controls.ServiceRouter
import po.db.data_service.scope.service.controls.ListBuilderL
import po.db.data_service.scope.service.controls.service_registry.ServiceRegistryItem
import po.db.data_service.scope.service.controls.service_registry.ServiceRegistryItemBuilder
import po.db.data_service.scope.service.controls.service_registry.ServiceUniqueKey
import po.db.data_service.scope.service.controls.service_registry.serviceRegistry

class ServiceClass<DATA_MODEL, ENTITY>(
    val connection :Database,
    val connectionName : String,
    val rootDataModel : DTOClass<DATA_MODEL, ENTITY>,
) where DATA_MODEL:DataModel,  ENTITY : LongEntity {


    fun <DATA_MODEL : Any, ENTITY : Any> ListBuilderL<ServiceRegistryItem<DATA_MODEL, ENTITY>>.addServiceRegistryItem(
        init: ServiceRegistryItemBuilder<DATA_MODEL, ENTITY>.() -> Unit
    ) = apply {
        val serviceRegistryItem = serviceRegistry<>(init)
        add(serviceRegistryItem)
    }

    val serviceRegistryList = ListBuilderL<ServiceRegistryItem<DTOClass<DATA_MODEL, ENTITY>, LongEntityClass<ENTITY>>>()
        .addServiceRegistryItem<DTOClass<DATA_MODEL, ENTITY>, LongEntityClass<ENTITY>> {
            key = ServiceUniqueKey("RootService")

            metadata {
                key = ServiceUniqueKey("MetadataKey")
                service {
                    rootDTOModel = CommonDTO::class
                    entityModel = ENTITY::class

                    DTOClass<DATA_MODEL, ENTITY> {
                        setEntityModel(DataModel::class)
                        setDTOModel(CommonDTO::class)
                    }
                }
            }
        }
        .list

    println(serviceRegistryList)

    val serviceRouter = ServiceRouter(connectionName, connection, serviceRegistry)


}
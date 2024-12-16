package po.db.data_service.scope.service

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.initializeDTO
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.exceptions.InitializationException
import po.db.data_service.scope.connection.controls.ServiceRouter
import po.db.data_service.scope.service.controls.ServiceRegistryBuilder
import po.db.data_service.scope.service.controls.service_registry.DTOData
import po.db.data_service.scope.service.controls.service_registry.ServiceUniqueKey

class ServiceClass<DATA_MODEL, ENTITY>(
    private val connection :Database,
    private val connectionName : String,
    private val rootDataModel : DTOClass<DATA_MODEL, ENTITY>,
) where DATA_MODEL:DataModel,  ENTITY : LongEntity {

    val serviceRouter = ServiceRouter<DATA_MODEL, ENTITY>(connectionName, connection)
    private val serviceRegistry = ServiceRegistryBuilder<DATA_MODEL, ENTITY>()

    init {
        try {
            initializeService()
        }catch (initException : InitializationException){
            println(initException.message)
        }
    }

    private fun initializeService(){
        serviceRegistry.addServiceRegistryItem {
            key = ServiceUniqueKey("TestRun")
            metadata {
                key = ServiceUniqueKey("TestRun")
                service {
                    rootDataModel.initialization() {
                        rootDTOModelData = it
                        childDTOModels
                    }
                }
            }
        }

//        rootDataModel.initialization().also { dtoClass->
//           dtoClass.dtoContext.also {
//              val dtoData =  DTOData(it.dtoModelClass, it.entityModel, it.dataModelClass)
//               serviceRegistry.addServiceRegistryItem {
//                   key = ServiceUniqueKey(dtoClass.dtoContext.name)
//                   metadata {
//                       key = ServiceUniqueKey(dtoClass.dtoContext.name)
//                       service {
//                           rootDTOModelData = dtoData
//                       }
//                   }
//               }
//            }
//        }
        val vb  = 10
    }

  //  val serviceRouter = ServiceRouter(connectionName, connection, serviceRegistry)
}
package po.db.data_service.scope.connection

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import po.db.data_service.dto.*
import po.db.data_service.dto.interfaces.DTOModelClass
import po.db.data_service.dto.interfaces.DTOModelV2
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.CommonDTO
import po.db.data_service.models.CommonDTOV2
import po.db.data_service.scope.connection.controls.ServiceCreateOptions
import po.db.data_service.scope.connection.controls.ServiceRouter
import po.db.data_service.scope.service.ServiceClass
import po.db.data_service.scope.service.ServiceContext
import po.db.data_service.scope.service.ServiceContextV2

class ConnectionContext(
    var connectionName: String,
    val connection: Database,
    val connectionClass :  ConnectionClass
) {

    fun <DTO : CommonDTOV2 >ConnectionContext.serviceV2(
        rootDtoModel : DTOClassV2,
        context: ServiceContextV2.()->Unit,
    ){
        try {
            ServiceClass(connection, rootDtoModel).let {
                connectionClass.addService(it)
                it.launch(context)
            }
        }catch (exception: Exception){
            println(exception.message)
            throw exception
        }
    }

    /**
     * Service initialization function
     */
    inline fun <DATA_MODEL, ENTITY>  ConnectionContext.service(
        rootDtoModel : DTOClass<DATA_MODEL, ENTITY>,
        createOptions: ServiceCreateOptions<DATA_MODEL, ENTITY>? = null,
        service: ServiceContext<DATA_MODEL, ENTITY>.() -> Unit
    ) where  DATA_MODEL : DataModel, ENTITY : LongEntity {

        val serviceRouter = ServiceRouter<DATA_MODEL, ENTITY>(connectionName, connection)
        val serviceName =  rootDtoModel::class.qualifiedName?:rootDtoModel::class.simpleName

//        serviceRouter.createService(name, rootDtoModel, this).let{ serviceContext->
//            serviceRouter.initializeRoute(ServiceUniqueKey(name), serviceContext).let {
//                service.invoke(it)
//            }
//        }
    }
}
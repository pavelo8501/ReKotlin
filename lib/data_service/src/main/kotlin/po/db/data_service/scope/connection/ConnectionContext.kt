package po.db.data_service.scope.connection

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import po.db.data_service.classes.DTOClass
import po.db.data_service.classes.interfaces.DataModel
import po.db.data_service.scope.service.ServiceClass
import po.db.data_service.scope.service.ServiceContext
import po.db.data_service.scope.service.TableCreateMode

class ConnectionContext(
    var connectionName: String,
    val connection: Database,
    private val connectionClass :  ConnectionClass
) {



    fun <DATA, ENTITY>ConnectionContext.service(
        rootDtoModel : DTOClass<DATA,ENTITY>,
        serviceCreateOption : TableCreateMode? = null,
        context: ServiceContext<DATA,ENTITY>.()->Unit,
    ) where DATA : DataModel,   ENTITY : LongEntity {
        try {
            ServiceClass(connection, rootDtoModel, serviceCreateOption).let {
                connectionClass.addService(it)
                it.launch(context)
            }
        }catch (exception: Exception){
            println(exception.message)
            throw exception
        }
    }

    fun <DATA: DataModel, ENTITY: LongEntity>getServiceContext(
        dtoModel : DTOClass<DATA, ENTITY>,
        context: ()->Unit,
    ): Boolean{
        connectionClass.getService(dtoModel.sourceClass.toString())?.let { serviceClass->
            serviceClass.relaunchServiceContext<DATA, ENTITY>(dtoModel, context)

            return true
        }
        return false
    }

    fun <DATA: DataModel, ENTITY: LongEntity> getServiceContextAttached(
        dtoModel : DTOClass<DATA, ENTITY>,
        context: ServiceContext<DATA,ENTITY>.()->Unit ): Boolean{
        connectionClass.getService("${dtoModel.sourceClass.simpleName}|Service")?.let { serviceClass->
            return serviceClass.attachToServiceContext<DATA, ENTITY>(dtoModel, context)
        }
        return false
    }

    fun <DATA: DataModel, ENTITY: LongEntity> getServiceContextAttachedSuppressed(
        dtoModel : DTOClass<DATA, ENTITY>,
        context: ServiceContext<DATA,ENTITY>.()->Unit ): Boolean{
        connectionClass.getService("${dtoModel.sourceClass.simpleName}|Service")?.let { serviceClass->
           return serviceClass.attachToServiceContextSuppressed<DATA, ENTITY>(dtoModel, context)
        }
        return false
    }

}
package po.exposify.scope.connection

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



    val isOpen : Boolean
        get(){return  connectionClass.isConnectionOpen }


    fun <DATA, ENTITY>ConnectionContext.service(
        rootDtoModel : DTOClass<DATA,ENTITY>,
        serviceCreateOption : TableCreateMode? = null,
        context: ServiceContext<DATA,ENTITY>.()->Unit,
    ) where DATA : DataModel,   ENTITY : LongEntity {
        try {
            ServiceClass(connectionClass, rootDtoModel, serviceCreateOption).let {
                connectionClass.addService(it)
                it.launch(context)
            }
        }catch (exception: Exception){
            println(exception.message)
            throw exception
        }
    }


    fun <DATA: DataModel, ENTITY: LongEntity> attachToContext(
        dtoModel : DTOClass<DATA, ENTITY>,
        context: ServiceContext<DATA,ENTITY>.()->Unit ): Boolean{
        connectionClass.getService("${dtoModel.sourceClass.simpleName}|Service")?.let { serviceClass->
           return serviceClass.attachToContext<DATA, ENTITY>(dtoModel, context)
        }
        return false
    }

}
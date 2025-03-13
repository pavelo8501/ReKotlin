package po.exposify.scope.connection

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.DataModel
import po.exposify.scope.service.ServiceClass
import po.exposify.scope.service.ServiceContext
import po.exposify.scope.service.TableCreateMode

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

    /**
     * Extends the given DTO model's service context within the hosting application.
     *
     * This function attempts to retrieve a service instance associated with the provided `dtoModel`.
     * If found, it invokes the `attachToContext` function on the service, extending its existing context.
     *
     * @param DATA The type of the data model.
     * @param ENTITY The type of the associated database entity, extending [LongEntity].
     * @param dtoModel The DTO model owning the service context
     **/
    fun <DATA: DataModel, ENTITY: LongEntity> attachToContext(
        dtoModel : DTOClass<DATA, ENTITY>,
        context: ServiceContext<DATA,ENTITY>.()->Unit ): Boolean{
        connectionClass.getService("${dtoModel.sourceClass.simpleName}|Service")?.let { serviceClass->
           return serviceClass.attachToContext<DATA, ENTITY>(dtoModel, context)
        }
        return false
    }

}
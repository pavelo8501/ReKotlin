package po.exposify.scope.connection

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import po.exposify.classes.interfaces.DataModel
import po.exposify.classes.DTOClass
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.service.ServiceClass
import po.exposify.scope.service.ServiceContext
import po.exposify.scope.service.enums.TableCreateMode

class ConnectionContext(
    var connectionName: String,
    val connection: Database,
    private val connectionClass :  ConnectionClass
) {

    val isOpen : Boolean
        get(){return  connectionClass.isConnectionOpen }

    fun <DTO, DATA> service(
        rootDtoModel : DTOClass<DTO>,
        serviceCreateOption : TableCreateMode = TableCreateMode.CREATE,
        context: ServiceContext<DTO, DATA>.()->Unit,
    ) where DTO : ModelDTO, DATA : DataModel {
        try {
           val serviceClass =  ServiceClass<DTO, DATA, LongEntity>(
                connectionClass,
                rootDtoModel,
                serviceCreateOption)

            connectionClass.addService(serviceClass)
            serviceClass.launch(context)

            serviceClass.let {
                    connectionClass.addService(it)
                    runBlocking {
                        it.launch(context)
                    }
                }
        }catch (exception: Exception){
            println(exception.message)
            throw exception
        }
    }

}
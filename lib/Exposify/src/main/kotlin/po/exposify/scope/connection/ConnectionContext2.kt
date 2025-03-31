package po.exposify.scope.connection

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.Database
import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.classes.DTOClass2
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.service.ServiceClass
import po.exposify.scope.service.ServiceClass2
import po.exposify.scope.service.ServiceContext
import po.exposify.scope.service.ServiceContext2
import po.exposify.scope.service.enums.TableCreateMode

class ConnectionContext2(
    var connectionName: String,
    val connection: Database,
    private val connectionClass :  ConnectionClass2
) {

    val isOpen : Boolean
        get(){return  connectionClass.isConnectionOpen }

    fun <DTO, DATA> service(
        rootDtoModel : DTOClass2<DTO>,
        serviceCreateOption : TableCreateMode? = null,
        context: ServiceContext2<DTO, DATA>.()->Unit,
    ) where DTO : ModelDTO, DATA : DataModel {
        try {
           val serviceClass =  ServiceClass2<DTO, DATA, LongEntity>(
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
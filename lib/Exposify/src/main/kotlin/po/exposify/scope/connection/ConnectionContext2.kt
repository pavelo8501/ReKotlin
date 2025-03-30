package po.exposify.scope.connection

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.dao.LongEntity
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

    fun <DTO>ConnectionContext2.service(
        rootDtoModel : DTOClass2<DTO>,
        serviceCreateOption : TableCreateMode? = null,
        context: suspend ServiceContext2<DTO>.()->Unit,
    ) where DTO : ModelDTO {
        try {
            ServiceClass2(connectionClass, rootDtoModel, serviceCreateOption).let {
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
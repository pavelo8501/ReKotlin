package po.exposify.scope.connection

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.RootDTO
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.service.ServiceClass
import po.exposify.scope.service.ServiceContext
import po.exposify.scope.service.enums.TableCreateMode
import po.lognotify.TasksManaged
import po.lognotify.extensions.startTaskAsync

class ConnectionContext(
    internal val connection: Database,
    private val connClass :  ConnectionClass
):  TasksManaged {

    val isOpen : Boolean
        get(){return  connClass.isConnectionOpen }

    suspend fun <DTO, DATA> service(
        dtoClass : RootDTO<DTO, DATA>,
        createOptions : TableCreateMode = TableCreateMode.CREATE,
        block: suspend ServiceContext<DTO, DATA, LongEntity>.()->Unit,
    ) where DTO : ModelDTO, DATA : DataModel{

        val serviceClass =  ServiceClass<DTO, DATA, LongEntity>(connClass, createOptions)
        startTaskAsync("Create Service") {
            serviceClass.startService(dtoClass, block)
        }.onComplete {
            connClass.addService(serviceClass)
        }
    }

    fun clearServices(){
        connClass.clearServices()
    }

}
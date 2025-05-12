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
import po.lognotify.extensions.newTaskAsync

class ConnectionContext(
    internal val connection: Database,
    private val connClass :  ConnectionClass
):  TasksManaged {

    val isOpen : Boolean
        get(){return  connClass.isConnectionOpen }

    suspend fun <DTO, DATA, E> service(
        dtoClass : RootDTO<DTO, DATA, E>,
        createOptions : TableCreateMode = TableCreateMode.CREATE,
        block: suspend ServiceContext<DTO, DATA, E>.()->Unit,
    ) where DTO : ModelDTO, DATA : DataModel, E: LongEntity {

        val serviceClass =  ServiceClass<DTO, DATA, E>(connClass, createOptions)
        newTaskAsync("Create Service", "ConnectionContext") {
            serviceClass.startService(dtoClass, block)
        }.onComplete {
            connClass.addService(serviceClass)
        }
    }

    fun clearServices(){
        connClass.clearServices()
    }

}
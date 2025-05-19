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
import po.lognotify.extensions.runTask
import po.lognotify.extensions.runTaskAsync
import po.lognotify.extensions.runTaskBlocking
import po.lognotify.lastTaskHandler

class ConnectionContext(
    internal val connection: Database,
    private val connClass :  ConnectionClass
):  TasksManaged {

    val isOpen : Boolean
        get(){return  connClass.isConnectionOpen }

    init {
        lastTaskHandler().info("Initialized")
    }

    fun <DTO, D, E> service(
        dtoClass : RootDTO<DTO, D, E>,
        createOptions : TableCreateMode = TableCreateMode.CREATE,
        block: suspend ServiceContext<DTO, D, E>.()->Unit,
    ) where DTO : ModelDTO, D: DataModel, E: LongEntity {

        val serviceClass = ServiceClass(dtoClass, connClass, createOptions)
        runTask("Create Service") { handler ->
            handler.info("Creating ServiceClass")
            serviceClass.qualifiedName
            connClass.addService(serviceClass)
            serviceClass.initService(dtoClass)

        }
        runTaskBlocking("Launch ServiceContext") { handler ->
            handler.info("Launching ServiceContext")
            connClass.getService<DTO, D, E>(serviceClass.qualifiedName)?.runServiceContext(block)
        }
    }

    fun clearServices(){
        connClass.clearServices()
    }

}
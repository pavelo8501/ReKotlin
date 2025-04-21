package po.exposify.scope.connection

import org.jetbrains.exposed.sql.Database
import po.exposify.classes.interfaces.DataModel
import po.exposify.classes.DTOClass
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
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

    fun <DTO, DATA> service(
        dtoClass : DTOClass<DTO>,
        createOptions : TableCreateMode = TableCreateMode.CREATE,
        context: ServiceContext<DTO, DATA>.()->Unit,
    ) where DTO : ModelDTO, DATA : DataModel{
       val serviceClass =  ServiceClass<DTO, DATA, ExposifyEntityBase>(connClass, dtoClass, createOptions)

        startTaskAsync("Create Service") {
            serviceClass.launch(context)
        }.onComplete {
            connClass.addService(serviceClass)
        }
    }

    fun clearServices(){
        connClass.clearServices()
    }

}
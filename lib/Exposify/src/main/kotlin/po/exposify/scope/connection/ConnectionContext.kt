package po.exposify.scope.connection

import org.jetbrains.exposed.sql.Database
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.RootDTO
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntity
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
        dtoClass : RootDTO<DTO, DATA>,
        createOptions : TableCreateMode = TableCreateMode.CREATE,
        block: ServiceContext<DTO, DATA, ExposifyEntity>.()->Unit,
    ) where DTO : ModelDTO, DATA : DataModel{

        val serviceClass =  ServiceClass<DTO, DATA, ExposifyEntity>(connClass, createOptions)
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
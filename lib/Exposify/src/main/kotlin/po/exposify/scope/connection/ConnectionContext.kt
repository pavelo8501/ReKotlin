package po.exposify.scope.connection

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import org.jetbrains.exposed.sql.transactions.transaction
import po.exposify.classes.interfaces.DataModel
import po.exposify.classes.DTOClass
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.extensions.safeCast
import po.exposify.scope.service.ServiceClass
import po.exposify.scope.service.ServiceContext
import po.exposify.scope.service.enums.TableCreateMode
import po.lognotify.TasksManaged
import po.lognotify.extensions.getOrThrowDefault
import po.lognotify.extensions.resultOrDefault
import po.lognotify.extensions.resultOrNull
import po.lognotify.extensions.startTask
import po.lognotify.extensions.startTaskAsync
import po.lognotify.extensions.subTask

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
    ) where DTO : ModelDTO, DATA : DataModel = startTaskAsync("Create Service") {
        suspendedTransactionAsync {
       val serviceClass =  ServiceClass<DTO, DATA, ExposifyEntityBase>(connClass, dtoClass, createOptions)

        val casted = serviceClass.safeCast<ServiceClass<ModelDTO, DataModel, ExposifyEntityBase>>()
            .getOrThrowDefault("Cast toServiceClass<ModelDTO, DataModel, ExposifyEntityBase> failed")
        connClass.addService(casted)
            serviceClass.launch(context)
        }.await()
    }.resultOrDefault(Unit)

}
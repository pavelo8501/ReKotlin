package po.exposify.scope.service

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.name
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import po.exposify.common.classes.exposedDebugger
import po.exposify.common.events.ContextEvent
import po.exposify.dto.interfaces.DataModel
import po.exposify.common.interfaces.AsContext
import po.exposify.dto.RootDTO
import po.exposify.dto.components.DeferredWhere
import po.exposify.dto.components.ExecutionProvider
import po.exposify.dto.components.SimpleQuery
import po.exposify.dto.components.WhereQuery
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.InitException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.withTransactionIfNone
import po.lognotify.TasksManaged
import po.lognotify.anotations.LogOnFault
import po.lognotify.classes.task.result.onFailureCause
import po.lognotify.debug.debugProxy
import po.lognotify.debug.extensions.captureProperty
import po.lognotify.extensions.runTask
import po.lognotify.extensions.runTaskBlocking
import po.misc.data.printable.printableProxy
import po.misc.exceptions.toManageable
import po.misc.interfaces.ClassIdentity
import po.misc.interfaces.IdentifiableClass
import po.misc.interfaces.IdentifiableContext

class ServiceContext<DTO, DATA, ENTITY>(
    @PublishedApi internal  val serviceClass : ServiceClass<DTO, DATA, ENTITY>,
    internal val dtoClass: RootDTO<DTO, DATA, ENTITY>,
): TasksManaged, IdentifiableClass where DTO : ModelDTO, DATA: DataModel,  ENTITY: LongEntity {

    override val identity: ClassIdentity = ClassIdentity.create("ServiceContext", dtoClass.identity.sourceName)
    private val executionProvider: ExecutionProvider<DTO, DATA, ENTITY> get() = dtoClass.executionContext
    private val dbConnection: Database get() = serviceClass.connection

    val debugger = exposedDebugger(this, ContextEvent){
        ContextEvent(this@ServiceContext, it.message)
    }

    fun truncate() = runTaskBlocking("Truncate") { handler ->
        newSuspendedTransaction {
            val table = dtoClass.getEntityModel().table
            try {
                exec("TRUNCATE TABLE ${table.tableName} RESTART IDENTITY CASCADE")
                handler.info("TRUNCATE TABLE ${table.tableName} RESTART IDENTITY CASCADE Executed")
            } catch (th: Throwable) {
               val managed =  th.toManageable<InitException, ExceptionCode>(this@ServiceContext, ExceptionCode.DB_TABLE_CREATION_FAILURE)
                handler.warn(managed)
            }
        }
    }.onFailureCause {
        val a = it
    }.resultOrException()

    fun pick(conditions: SimpleQuery): ResultSingle<DTO, DATA, ENTITY> =
        runTask("Pick(conditions)", debugProxy = debugger.capture(conditions){ captureProperty<SimpleQuery>(parameter)}) {handler->
        withTransactionIfNone(debugger, false){
            executionProvider.pick(conditions)
        }
    }.resultOrException()

    fun <T : IdTable<Long>> pick(conditions: WhereQuery<T>): ResultSingle<DTO, DATA, ENTITY> =
        runTask("Pick(conditions)", debugProxy = debugger.capture(conditions){ captureProperty<SimpleQuery>(parameter)}) {handler->
            withTransactionIfNone(debugger, false) {
                executionProvider.pick(conditions)
            }
        }.resultOrException()

    fun pickById(id: Long): ResultSingle<DTO, DATA, ENTITY> =
        runTask("Pick(id)", debugProxy = debugger.captureInput(id)) { handler ->
        withTransactionIfNone(debugger, false) {
            executionProvider.pickById(id, this)
        }
    }.resultOrException()

    fun select(): ResultList<DTO, DATA, ENTITY> =
        runTask("Select",  debugProxy = debugger.captureInput()) {handler->
        withTransactionIfNone(debugger, false) {
            executionProvider.select()
        }
    }.resultOrException()

    fun <T : IdTable<Long>> select(conditions: DeferredWhere<T>):ResultList<DTO, DATA, ENTITY> =
        runTask("Select(with conditions)", debugProxy = debugger.capture(conditions) { captureProperty(conditions) } ) {handler->
            withTransactionIfNone(debugger, false) {
                executionProvider.select(conditions.resolve())
            }
        }.resultOrException()

    fun update(dataModel: DATA): ResultSingle<DTO, DATA, ENTITY> =
        runTask("Update", debugProxy = debugger.capture<DataModel>(dataModel){ captureProperty(dataModel.id) } ) { handler ->
        withTransactionIfNone(debugger, false) {
            executionProvider.update(dataModel, dtoClass)
        }
    }.resultOrException()

    fun update(dataModels: List<DATA>): ResultList<DTO, DATA, ENTITY> =
        runTask("Update", debugProxy = debugger.capture<List<DataModel>>(dataModels){ captureProperty(parameter) } ) { handler ->
        withTransactionIfNone(debugger, false) {
            executionProvider.update(dataModels, dtoClass)
        }
    }.resultOrException()

    fun insert(dataModels: List<DATA>): ResultList<DTO, DATA, ENTITY> =
        runTask("Insert", debugProxy = debugger.capture<List<DataModel>>(dataModels){ captureProperty(parameter) } ) { handler ->
            withTransactionIfNone(debugger, false) {
                executionProvider.insert(dataModels)
            }
        }.resultOrException()

    fun delete(toDelete: DATA): ResultList<DTO, DATA, ENTITY>? =
        runTask("Delete", debugProxy =  debugger.capture<DataModel>(toDelete) { captureProperty(parameter) }) {handler->
        withTransactionIfNone(debugger, false) {

            executionProvider.delete(toDelete)
        }
        null
    }.resultOrException()

}
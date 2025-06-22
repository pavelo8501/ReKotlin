package po.exposify.scope.service

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.name
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import po.exposify.dto.interfaces.DataModel
import po.exposify.common.interfaces.AsContext
import po.exposify.dto.RootDTO
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
import po.lognotify.extensions.runTask
import po.lognotify.extensions.runTaskBlocking
import po.misc.exceptions.toManageable
import po.misc.interfaces.IdentifiableContext

class ServiceContext<DTO, DATA, ENTITY>(
    @PublishedApi internal  val serviceClass : ServiceClass<DTO, DATA, ENTITY>,
    internal val dtoClass: RootDTO<DTO, DATA, ENTITY>,
): TasksManaged, IdentifiableContext,  AsContext<DATA>  where DTO : ModelDTO, DATA: DataModel,  ENTITY: LongEntity {

    private val dbConnection: Database get() = serviceClass.connection

    override val contextName: String = "ServiceContext"

    @LogOnFault
    val personalName: String = "ServiceContext[${dbConnection.name}]"

    private val executionProvider: ExecutionProvider<DTO, DATA, ENTITY> by lazy { ExecutionProvider(dtoClass) }

    fun truncate() = runTaskBlocking("Truncate") { handler ->
        newSuspendedTransaction {
            val table = dtoClass.getEntityModel().table
            try {
                exec("TRUNCATE TABLE ${table.tableName} RESTART IDENTITY CASCADE")
                handler.info("TRUNCATE TABLE ${table.tableName} RESTART IDENTITY CASCADE Executed")
            } catch (th: Throwable) {
               val managed =  th.toManageable<InitException>(this@ServiceContext, ExceptionCode.DB_TABLE_CREATION_FAILURE)
                handler.warn(managed)
            }
        }
    }.onFailureCause {
        val a = it
    }.resultOrException()

    fun pick(conditions: SimpleQuery): ResultSingle<DTO, DATA, ENTITY>
        = runTask("Pick by conditions") {handler->
        withTransactionIfNone(handler){
            executionProvider.pick(conditions)
        }
    }.resultOrException()

    fun <T : IdTable<Long>> pick(conditions: WhereQuery<T>): ResultSingle<DTO, DATA, ENTITY> =
        runTask("Pick by conditions") {handler->
            withTransactionIfNone(handler) {
                executionProvider.pick(conditions)
            }
        }.resultOrException()

    fun pickById(id: Long): ResultSingle<DTO, DATA, ENTITY> = runTask("Pick by ID") { handler ->
        withTransactionIfNone(handler) {
            executionProvider.pickById(id)
        }
    }.resultOrException()

    fun select(invalidateCache: Boolean = false): ResultList<DTO, DATA, ENTITY> = runTask("Select") {handler->
        withTransactionIfNone(handler) {
            executionProvider.select(invalidateCache)
        }
    }.resultOrException()

    fun <T : IdTable<Long>> select(conditions: WhereQuery<T>): ResultList<DTO, DATA, ENTITY> =
        runTaskBlocking("Select With Conditions") {handler->
            withTransactionIfNone(handler) {
                executionProvider.select(conditions)
            }
        }.resultOrException()

    fun update(dataModel: DATA): ResultSingle<DTO, DATA, ENTITY> = runTask("Update") { handler ->
        withTransactionIfNone(handler) {
            executionProvider.update(dataModel)
        }
    }.resultOrException()

    fun update(dataModels: List<DATA>): ResultList<DTO, DATA, ENTITY> = runTask("Update") { handler ->
        withTransactionIfNone(handler) {
            executionProvider.update(dataModels)
        }
    }.resultOrException()

    fun delete(toDelete: DATA): ResultList<DTO, DATA, ENTITY>? = runTaskBlocking("Delete") {handler->
        withTransactionIfNone(handler) {
            executionProvider.update(toDelete)
        }
        null
    }.resultOrException()

}
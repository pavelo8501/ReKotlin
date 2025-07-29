package po.exposify.scope.service

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import po.exposify.common.classes.exposifyDebugger
import po.exposify.common.events.ContextData
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.RootDTO
import po.exposify.dto.components.ExecutionContext
import po.exposify.dto.components.query.SimpleQuery
import po.exposify.dto.components.query.WhereQuery
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.InitException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.withTransactionRestored
import po.lognotify.TasksManaged
import po.lognotify.common.result.onFailureCause
import po.lognotify.extensions.runTask
import po.lognotify.extensions.runTaskBlocking
import po.misc.context.CTXIdentity
import po.misc.context.asSubIdentity
import po.misc.exceptions.throwableToText
import po.misc.functions.containers.DeferredContainer


class ServiceContext<DTO, DATA, ENTITY>(
    @PublishedApi internal  val serviceClass : ServiceClass<DTO, DATA, ENTITY>,
    val dtoClass: RootDTO<DTO, DATA, ENTITY>,
): TasksManaged where DTO : ModelDTO, DATA: DataModel,  ENTITY: LongEntity {


    override val identity: CTXIdentity<ServiceContext<DTO, DATA, ENTITY>> = asSubIdentity(this, dtoClass)

    private val executionProvider: ExecutionContext<DTO, DATA, ENTITY> get() = dtoClass.executionContext
    private val dbConnection: Database get() = serviceClass.connection

    val debugger = exposifyDebugger(this, ContextData){
        ContextData(it.message)
    }

    fun truncate() = runTaskBlocking("Truncate") { handler ->
        dtoClass.clearCachedDTOs()
        newSuspendedTransaction {
            val table = dtoClass.entityClass.table
            try {
                exec("TRUNCATE TABLE ${table.tableName} RESTART IDENTITY CASCADE")
                handler.info("TRUNCATE TABLE ${table.tableName} RESTART IDENTITY CASCADE Executed")
            } catch (th: Throwable) {

                val managed = InitException(th.message.toString(),  ExceptionCode.DB_TABLE_CREATION_FAILURE, this@ServiceContext , th)
                handler.warn(managed)
            }
        }
    }.resultOrException()

    fun pick(conditions: SimpleQuery): ResultSingle<DTO, DATA, ENTITY> =
        runTask("Pick(conditions)") {
            withTransactionRestored(debugger, false){
            executionProvider.pick(conditions)
        }
    }.resultOrException()

    fun pick(conditions: DeferredContainer<WhereQuery<ENTITY>>): ResultSingle<DTO, DATA, ENTITY> =
        runTask("Pick(conditions)") {
            withTransactionRestored (debugger, false) {
                executionProvider.pick(conditions.resolve())
            }
        }.resultOrException()

    fun pickById(id: Long): ResultSingle<DTO, DATA, ENTITY> =
        runTask("Pick(id)") {
            withTransactionRestored(debugger, false) {
                executionProvider.pickById(id)
            }
    }.resultOrException()

    fun select(): ResultList<DTO, DATA, ENTITY> =
        runTask("Select") {
            withTransactionRestored(debugger, false) {
            executionProvider.select()
        }
    }.resultOrException()

    fun select(conditions: DeferredContainer<WhereQuery<ENTITY>>):ResultList<DTO, DATA, ENTITY> =
        runTask("Select(with conditions)") {
            withTransactionRestored(debugger, false) {
                executionProvider.select(conditions.resolve())
            }
        }.resultOrException()

    fun update(dataModel: DATA): ResultSingle<DTO, DATA, ENTITY> =
        runTask("Update") {



            withTransactionRestored(debugger, false) {
            executionProvider.update(dataModel, dtoClass)
        }

    }.onFailureCause { exception->
            println(exception.throwableToText())
    } .resultOrException()

    fun update(dataModels: List<DATA>): ResultList<DTO, DATA, ENTITY> =
        runTask("Update") {
            withTransactionRestored(debugger, false) {
            executionProvider.update(dataModels, dtoClass)
        }
    }.resultOrException()

    fun insert(dataModels: List<DATA>): ResultList<DTO, DATA, ENTITY> =
        runTask("Insert") {
            withTransactionRestored(debugger, false) {
                executionProvider.insert(dataModels)
            }
        }.resultOrException()

    fun delete(toDelete: DATA): ResultList<DTO, DATA, ENTITY>? =
        runTask("Delete"){
            withTransactionRestored(debugger, false) {
            executionProvider.delete(toDelete)
        }
        null
    }.resultOrException()

}
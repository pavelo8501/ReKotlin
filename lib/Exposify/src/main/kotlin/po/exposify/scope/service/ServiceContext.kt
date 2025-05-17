package po.exposify.scope.service

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.name
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import po.exposify.dto.interfaces.DataModel
import po.exposify.common.interfaces.AsContext
import po.exposify.dto.RootDTO
import po.exposify.dto.components.SimpleQuery
import po.exposify.dto.components.ResultList
import po.exposify.dto.components.ResultSingle
import po.exposify.dto.components.RootExecutionProvider
import po.exposify.dto.components.WhereQuery
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.withTransactionIfNone
import po.lognotify.TasksManaged
import po.lognotify.anotations.LogOnFault
import po.lognotify.extensions.newTaskAsync
import po.lognotify.extensions.onFailureCause

class ServiceContext<DTO, DATA, ENTITY>(
    internal  val serviceClass : ServiceClass<DTO, DATA, ENTITY>,
    internal val dtoClass: RootDTO<DTO, DATA, ENTITY>,
): TasksManaged,  AsContext<DATA>  where DTO : ModelDTO, DATA: DataModel,  ENTITY: LongEntity{

    private val dbConnection: Database get()= serviceClass.connection
    @LogOnFault
    val personalName: String = "ServiceContext[${dbConnection.name}]"

    private val executionProvider: RootExecutionProvider<DTO, DATA, ENTITY> by lazy { RootExecutionProvider(dtoClass)}

    fun truncate()
        = newTaskAsync("Truncate", personalName) {handler->
        newSuspendedTransaction {
            val table = dtoClass.getEntityModel().table
            try {
                exec("TRUNCATE TABLE ${table.tableName} RESTART IDENTITY CASCADE")
                handler.info("TRUNCATE TABLE ${table.tableName} RESTART IDENTITY CASCADE Executed")
            }catch (th: Throwable){
                handler.warn(th, "Running TRUNCATE TABLE ${table.tableName} RESTART IDENTITY CASCADE")
            }
        }
    }.onFailureCause {
        val a = it
    }.resultOrException()

    fun pick(conditions: SimpleQuery): ResultSingle<DTO, DATA, ENTITY>
            = newTaskAsync("Pick by conditions", personalName) {
        withTransactionIfNone {
            executionProvider.pick(conditions)
        }
    }.resultOrException()

    fun <T: IdTable<Long>>pick(conditions: WhereQuery<T>): ResultSingle<DTO, DATA, ENTITY>
         = newTaskAsync("Pick by conditions", personalName) {
            withTransactionIfNone {
                executionProvider.pick(conditions)
            }
        }.resultOrException()

    fun pickById(id: Long): ResultSingle<DTO, DATA, ENTITY>
        = newTaskAsync("Pick by ID", personalName) {handler->
            withTransactionIfNone(handler) {
                executionProvider.pickById(id)
            }
        }.resultOrException()


    fun select(): ResultList<DTO, DATA, ENTITY>
         =  newTaskAsync("Select", personalName) {
            withTransactionIfNone {
                executionProvider.select()
            }
        }.resultOrException()

    fun <T: IdTable<Long>> select(conditions:  WhereQuery<T>): ResultList<DTO, DATA, ENTITY>
         = newTaskAsync("Select With Conditions", personalName) {
            withTransactionIfNone {
                executionProvider.select(conditions)
            }
        }.resultOrException()

    fun update(dataModel : DATA): ResultSingle<DTO,DATA, ENTITY>
      = newTaskAsync("Update", personalName) {handler->
            withTransactionIfNone {
                executionProvider.update(dataModel)
            }
        }.resultOrException()

    fun update(dataModels : List<DATA>): ResultList<DTO,DATA, ENTITY>
       =  newTaskAsync("Update", personalName) {handler->
            withTransactionIfNone(handler) {
                executionProvider.update(dataModels)
            }
    }.resultOrException()

    fun delete(toDelete: DATA): ResultList<DTO, DATA, ENTITY>?
       =  newTaskAsync("Delete", personalName) {
            withTransactionIfNone{
                executionProvider.update(toDelete)
            }
        null
        }.resultOrException()

//    fun sequence(
//        sequenceID : SequenceID,
//        block: suspend SequenceContext<DTO, DATA, ENTITY>.(RootSequenceHandler<DTO, DATA, ENTITY>) -> Unit
//    ){
//        val pack = RootSequencePack(dtoClass.generateKey(sequenceID), dtoClass, block)
//        serviceClass.addSequencePack(pack.key, pack)
//    }

}
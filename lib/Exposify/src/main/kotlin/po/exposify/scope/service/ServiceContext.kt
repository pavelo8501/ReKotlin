package po.exposify.scope.service

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import po.exposify.dto.interfaces.DataModel
import po.exposify.common.interfaces.AsContext
import po.exposify.dto.RootDTO
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.ResultList
import po.exposify.dto.components.ResultSingle
import po.exposify.dto.components.RootExecutionProvider
import po.exposify.dto.components.WhereQuery
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.withTransactionIfNone
import po.exposify.scope.sequence.SequenceContext
import po.exposify.scope.sequence.classes.RootSequenceHandler
import po.exposify.scope.sequence.enums.SequenceID
import po.exposify.scope.sequence.models.RootSequencePack
import po.lognotify.TasksManaged
import po.lognotify.anotations.LogOnFault
import po.lognotify.extensions.newTaskAsync
import po.misc.collections.generateKey

class ServiceContext<DTO, DATA, ENTITY: LongEntity>(
    internal  val serviceClass : ServiceClass<DTO, DATA, ENTITY>,
    internal val dtoClass: RootDTO<DTO, DATA, ENTITY>,
): TasksManaged,  AsContext<DATA>  where DTO : ModelDTO, DATA: DataModel{

    @LogOnFault
    val personalName: String = "ServiceContext[${dtoClass.config.registry.dtoName}]"

    private val dbConnection: Database = serviceClass.connection
    internal val dtoMap : MutableMap<Long, CommonDTO<DTO, DATA, ENTITY>> = mutableMapOf()

    private  val executionProvider = RootExecutionProvider(dtoClass)

    fun truncate(): Unit?
        = newTaskAsync("Truncate", personalName) {
            val entityModel = dtoClass.getEntityModel()
            val table = entityModel.table
            suspendedTransactionAsync {
                exec("TRUNCATE TABLE ${table.tableName} RESTART IDENTITY CASCADE")
            }.await()
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

    fun sequence(
        sequenceID : SequenceID,
        block: suspend SequenceContext<DTO, DATA, ENTITY>.(RootSequenceHandler<DTO, DATA, ENTITY>) -> Unit
    ){
        val pack = RootSequencePack(dtoClass.generateKey(sequenceID), dtoClass, block)
        serviceClass.addSequencePack(pack.key, pack)
    }

}
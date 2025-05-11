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
import po.exposify.scope.sequence.SequenceContext
import po.exposify.scope.sequence.classes.RootSequenceHandler
import po.exposify.scope.sequence.enums.SequenceID
import po.exposify.scope.sequence.models.RootSequencePack
import po.lognotify.TasksManaged
import po.lognotify.extensions.startTaskAsync
import po.misc.collections.generateKey

class ServiceContext<DTO, DATA, ENTITY: LongEntity>(
    internal  val serviceClass : ServiceClass<DTO, DATA, ENTITY>,
    internal val dtoClass: RootDTO<DTO, DATA, ENTITY>,
): TasksManaged,  AsContext<DATA>  where DTO : ModelDTO, DATA: DataModel{

    val personalName: String = "ServiceContext[${dtoClass.config.registry.dtoName}]"

    private val dbConnection: Database = serviceClass.connection
    internal val dtoMap : MutableMap<Long, CommonDTO<DTO, DATA, ENTITY>> = mutableMapOf()

    private  val executionProvider = RootExecutionProvider(dtoClass)

    fun truncate(){
        startTaskAsync("Truncate", personalName) {
            val entityModel = dtoClass.getEntityModel()
            val table = entityModel.table
            suspendedTransactionAsync {
                exec("TRUNCATE TABLE ${table.tableName} RESTART IDENTITY CASCADE")
            }.await()
        }
    }

    fun <T: IdTable<Long>>pick(conditions: WhereQuery<T>): ResultSingle<DTO, DATA, ENTITY>{
        val result =  startTaskAsync("Pick by conditions", personalName) {
            suspendedTransactionAsync {
                //dtoClass.pick<DTO, DATA, ENTITY, T>(conditions)
                executionProvider.pick(conditions)
            }.await()
        }.resultOrException()
        return result
    }

    fun  pick(id: Long): ResultSingle<DTO, DATA, ENTITY>{
        val result =  startTaskAsync("Pick by ID", personalName) {
            suspendedTransactionAsync {
                //dtoClass.pickById<DTO, DATA, ENTITY>(id)
                executionProvider.pickById(id)
            }.await()
        }.resultOrException()
        return result
    }

    fun select(): ResultList<DTO, DATA, ENTITY> {
        val result =  startTaskAsync("Select", personalName) {
            suspendedTransactionAsync {
                executionProvider.select()
            }.await()
        }.resultOrException()
        return result
    }

    fun <T: IdTable<Long>> select(conditions:  WhereQuery<T>): ResultList<DTO, DATA, ENTITY>{
        val crudResult =  startTaskAsync("Select With Conditions", personalName) {
            suspendedTransactionAsync {
                //dtoClass.select<T, DTO, DATA, ENTITY>(conditions)
                executionProvider.select(conditions)
            }.await()
        }.resultOrException()
        return crudResult
    }

    fun update(dataModel : DATA): ResultSingle<DTO,DATA, ENTITY> {
        val result =  startTaskAsync("Update", personalName) {
            suspendedTransactionAsync {
              //  dtoClass.update<DTO, DATA, ENTITY>(dataModel)
                executionProvider.update(dataModel)
            }.await()
        }.resultOrException()
        return result
    }

    fun update(dataModels : List<DATA>): ResultList<DTO,DATA, ENTITY> {
       val result =  startTaskAsync("Update", personalName) {
            suspendedTransactionAsync {
               // dtoClass.update<DTO, DATA, ENTITY>(dataModels)
                executionProvider.update(dataModels)
            }.await()
        }.resultOrException()
       return result
    }

    fun delete(toDelete: DATA): ResultList<DTO, DATA, ENTITY>?{
        val result =  startTaskAsync("Delete", personalName) {
            suspendedTransactionAsync {
               // dtoClass.delete(toDelete)
                executionProvider.update(toDelete)
            }.await()
        }.resultOrException()
        return  null
    }

    fun sequence(
        sequenceID : SequenceID,
        block: suspend SequenceContext<DTO, DATA, ENTITY>.(RootSequenceHandler<DTO, DATA, ENTITY>) -> Unit
    ){
        val pack = RootSequencePack(dtoClass.generateKey(sequenceID), dtoClass, block)
        serviceClass.addSequencePack(pack.key, pack)
    }

}
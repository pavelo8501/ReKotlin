package po.exposify.scope.service

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import po.exposify.dto.interfaces.DataModel
import po.exposify.common.interfaces.AsContext
import po.exposify.dto.RootDTO
import po.exposify.dto.extensions.delete
import po.exposify.dto.extensions.pick
import po.exposify.dto.extensions.pickById
import po.exposify.dto.extensions.select
import po.exposify.dto.extensions.update
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.ResultList
import po.exposify.dto.components.ResultSingle
import po.exposify.dto.components.WhereQuery
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntity
import po.exposify.extensions.castOrInitEx
import po.exposify.scope.sequence.SequenceContext
import po.exposify.scope.sequence.classes.SequenceHandler

import po.exposify.scope.sequence.models.SequencePack
import po.lognotify.TasksManaged
import po.lognotify.extensions.startTaskAsync
import po.misc.collections.generateKey

class ServiceContext<DTO, DATA, ENTITY: LongEntity>(
    internal  val serviceClass : ServiceClass<DTO, DATA, ENTITY>,
    internal val dtoClass: RootDTO<DTO, DATA>,
): TasksManaged,  AsContext<DATA>  where DTO : ModelDTO, DATA: DataModel{

    val personalName: String = "ServiceContext[${dtoClass.config.registry.dtoName}]"

    private val dbConnection: Database = serviceClass.connection
    internal val dtoMap : MutableMap<Long, CommonDTO<DTO, DATA, ENTITY>> = mutableMapOf()

    fun truncate(){
        startTaskAsync("Truncate", personalName) {
            val entityModel = dtoClass.getEntityModel<ExposifyEntity>()
            val table = entityModel.table
            suspendedTransactionAsync {
                exec("TRUNCATE TABLE ${table.tableName} RESTART IDENTITY CASCADE")
            }.await()
        }
    }

    fun <T: IdTable<Long>>pick(conditions: WhereQuery<T>): ResultSingle<DTO, DATA>{
        val result =  startTaskAsync("Pick by conditions", personalName) {
            suspendedTransactionAsync {
                dtoClass.pick<DTO, DATA, T>(conditions)
            }.await()
        }.resultOrException()
        return result
    }

    fun  pick(id: Long): ResultSingle<DTO, DATA>{
        val result =  startTaskAsync("Pick by ID", personalName) {
            suspendedTransactionAsync {
                dtoClass.pickById<DTO, DATA>(id)
            }.await()
        }.resultOrException()
        return result
    }

    fun select(): ResultList<DTO, DATA> {
        val result =  startTaskAsync("Select", personalName) {
            suspendedTransactionAsync {
                dtoClass.select()
            }.await()
        }.resultOrException()
        return result
    }

    fun <T: IdTable<Long>> select(conditions:  WhereQuery<T>): ResultList<DTO, DATA>{
        val crudResult =  startTaskAsync("Select With Conditions", personalName) {
            suspendedTransactionAsync {
                dtoClass.select<T, DTO, DATA>(conditions)
            }.await()
        }.resultOrException()
        return crudResult
    }

    fun update(dataModel : DATA): ResultSingle<DTO,DATA> {
        val result =  startTaskAsync("Update", personalName) {
            suspendedTransactionAsync {
                dtoClass.update<DTO, DATA>(dataModel)
            }.await()
        }.resultOrException()
        return result
    }

    fun update(dataModels : List<DATA>): ResultList<DTO,DATA> {
       val result =  startTaskAsync("Update", personalName) {
            suspendedTransactionAsync {
                dtoClass.update<DTO, DATA>(dataModels)
            }.await()
        }.resultOrException()
       return result
    }

    fun delete(toDelete: DATA): ResultList<DTO, DATA>?{
        val result =  startTaskAsync("Delete", personalName) {
            suspendedTransactionAsync {
                dtoClass.delete(toDelete)
            }.await()
        }.resultOrException()
        return  result
    }

//    fun <R_DTO: ModelDTO, R_DATA: DataModel> sequence(
//        handler: SequenceHandler<DTO, DATA>,
//        block: suspend SequenceContext<DTO, DATA, ExposifyEntity>.(inputData: List<DATA>?, conditions: WhereCondition<IdTable<Long>>?) -> CrudResult<R_DTO, R_DATA>
//    ) {
//
//        val casted = serviceClass.castOrInitEx<ServiceClass<DTO, DATA, ExposifyEntity>>()
//        val sequenceContext = SequenceContext<DTO, DATA, ExposifyEntity>(dtoClass, handler)
//        val pack = SequencePack(sequenceContext, casted, block, handler)
//        serviceClass.addSequencePack(pack)
//    }

    suspend fun sequence(
        handler: SequenceHandler<DTO, DATA>,
        block: suspend SequenceContext<DTO, DATA, ExposifyEntity>.(SequenceHandler<DTO, DATA>) -> Unit
    ){
        val pack = SequencePack(block, handler)
        serviceClass.addSequencePack(pack)

    }


}
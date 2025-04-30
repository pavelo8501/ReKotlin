package po.exposify.scope.service

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import po.exposify.classes.interfaces.DataModel
import po.exposify.common.interfaces.AsContext
import po.exposify.dto.components.CrudResult
import po.exposify.classes.DTOClass
import po.exposify.classes.extensions.delete
import po.exposify.classes.extensions.pick
import po.exposify.classes.extensions.pickById
import po.exposify.classes.extensions.select
import po.exposify.classes.extensions.update
import po.exposify.dto.components.CrudResultSingle
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntity
import po.exposify.extensions.WhereCondition
import po.exposify.scope.sequence.SequenceContext
import po.exposify.scope.sequence.classes.SequenceHandler
import po.exposify.scope.sequence.models.SequencePack
import po.lognotify.TasksManaged
import po.lognotify.extensions.startTaskAsync

class ServiceContext<DTO, DATA>(
    private  val serviceClass : ServiceClass<DTO, DATA, ExposifyEntity>,
    internal val dtoClass: DTOClass<DTO>,
): TasksManaged,  AsContext<DATA>  where DTO : ModelDTO, DATA: DataModel{

    val personalName: String = "ServiceContext[${dtoClass.registryItem.dtoName}]"

    private val dbConnection: Database = serviceClass.connection
    init { dtoClass.asHierarchyRoot(this) }

    internal fun serviceClass():ServiceClass<DTO, DATA, ExposifyEntity>{
        return serviceClass
    }

    fun truncate(){
        startTaskAsync("Truncate", personalName) {
            val entityModel = dtoClass.getEntityModel<ExposifyEntity>()
            val table = entityModel.table
            suspendedTransactionAsync {
                exec("TRUNCATE TABLE ${table.tableName} RESTART IDENTITY CASCADE")
            }.await()
        }
    }

    fun <T: IdTable<Long>>pick(conditions: WhereCondition<T>): CrudResultSingle<DTO, DATA>{
        val result =  startTaskAsync("Pick by conditions", personalName) {
            suspendedTransactionAsync {
                dtoClass.pick<DTO, DATA, T>(conditions)
            }.await()
        }.resultOrException()
        return result
    }

    fun pick(id: Long): CrudResultSingle<DTO, DATA>{
        val result =  startTaskAsync("Pick by ID", personalName) {
            suspendedTransactionAsync {
                dtoClass.pickById<DTO, DATA>(id)
            }.await()
        }.resultOrException()
        return result
    }

    fun select(): CrudResult<DTO, DATA> {
        val result =  startTaskAsync("Select", personalName) {
            suspendedTransactionAsync {
                dtoClass.select<DTO, DATA>()
            }.await()
        }.resultOrException()
        return result
    }

    fun <T: IdTable<Long>> select(conditions:  WhereCondition<T>): CrudResult<DTO, DATA>{
        val crudResult =  startTaskAsync("Select With Conditions", personalName) {
            suspendedTransactionAsync {
                dtoClass.select<T, DTO, DATA>(conditions)
            }.await()
        }.resultOrException()
        return crudResult
    }

    fun update(dataModel : DATA): CrudResultSingle<DTO,DATA> {
        val result =  startTaskAsync("Update", personalName) {
            suspendedTransactionAsync {
                dtoClass.update(dataModel)
            }.await()
        }.resultOrException()
        return result
    }

    fun update(dataModels : List<DATA>): CrudResult<DTO,DATA> {
       val result =  startTaskAsync("Update", personalName) {
            suspendedTransactionAsync {
                dtoClass.update(dataModels)
            }.await()
        }.resultOrException()
       return result
    }

    fun delete(toDelete: DATA): CrudResult<DTO, DATA>?{
        val result =  startTaskAsync("Delete", personalName) {
            suspendedTransactionAsync {
                dtoClass.delete(toDelete)
            }.await()
        }.resultOrException()
        return  result
    }

    fun sequence(
        handler: SequenceHandler<DTO, DATA>,
        block: suspend SequenceContext<DTO, DATA>.(inputData: List<DATA>, conditions: WhereCondition<IdTable<Long>>?) -> Unit
    ) {
        val sequenceContext = SequenceContext<DTO, DATA>(serviceClass, dtoClass, handler)
        val pack = SequencePack(sequenceContext, serviceClass, block, handler)
        serviceClass.addSequencePack(pack)
    }


}
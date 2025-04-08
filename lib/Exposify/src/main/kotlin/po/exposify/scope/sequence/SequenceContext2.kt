package po.exposify.scope.sequence

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.components.CrudResult
import po.exposify.dto.CommonDTO
import po.exposify.classes.DTOClass
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.extensions.QueryConditions
import po.exposify.scope.dto.DTOContext2
import po.exposify.scope.sequence.classes.SequenceHandler2


class SequenceContext2<DTO>(
    private val connection: Database,
    private val hostDto : DTOClass<DTO>,
    private val handler : SequenceHandler2<DTO>
) where  DTO : ModelDTO
{

    private fun  <T>dbQuery(body : () -> T): T = transaction(connection) {
        body()
    }

    private var lastResult : CrudResult<DTO, DataModel>? = null

    private fun dtos(): List<CommonDTO<DTO, DataModel , ExposifyEntityBase>>{
        val result =   mutableListOf<CommonDTO<DTO, DataModel, ExposifyEntityBase>>()
        lastResult?.rootDTOs?.forEach{  result.add(it) }
        return result
    }


    fun getParam(key: String): String{
        val sequence = handler.getStockSequence()
        return sequence.getParam(key)
    }

    fun getInputList(): List<DataModel>{
        return  handler.getStockSequence().getInputList()
    }


//    fun <SWITCH_DATA: DataModel, SWITCH_ENTITY : LongEntity> DTOClass<SWITCH_DATA, SWITCH_ENTITY>.switch(
//        block:  SequenceContext<SWITCH_DATA, SWITCH_ENTITY>.(dtos: List<CommonDTO<SWITCH_DATA, SWITCH_ENTITY>>)->Unit ){
//        val list = dtos().map { it.getChildren<SWITCH_DATA, SWITCH_ENTITY>(this) }.flatten()
//        val result =  CrudResult<SWITCH_DATA, SWITCH_ENTITY>(list, null)
//
//        val newSequenceContext =  SequenceContext<SWITCH_DATA, SWITCH_ENTITY>(
//            connection,
//            this,
//            handler as SequenceHandler<SWITCH_DATA, SWITCH_ENTITY>
//        )
//        newSequenceContext.block(list)
//    }


    /**
     * Checks out current Sequence operations returning a deferred result
     *
     * @param withResult Optional result of the previous CRUD operation. If not provided,
     * last operation's result is used
     *
     * @return A `Deferred` list of `DATA` models.
     */
    suspend fun checkout(withResult :  CrudResult<DTO, DataModel> ? = null): Deferred<List<DataModel>> {
        val context = DTOContext2<DTO, DataModel>(hostDto, withResult ?: lastResult)
        return CompletableDeferred(context.getData())
    }

    /**
     * Selects data from the database based on the provided query conditions.
     *
     * If a lambda function (`block`) is provided, it processes the selected data inside a `SequenceContext`,
     * allowing further transformations or computations.
     * If no lambda is provided, the function immediately returns a `Deferred` result.
     *
     * @param T The table type extending `IdTable<Long>`.
     * @param conditions Optional query conditions to filter the selection. If `null`, all data is selected.
     * @param block Optional suspend lambda function executed within a `SequenceContext`,
     *              allowing further processing of the selected DTOs before finalizing execution.
     */
    suspend fun <T: IdTable<Long>> select(
        conditions: QueryConditions<T>? = null,
        block: (suspend SequenceContext2<DTO>.(dtos: List<CommonDTO<DTO, *, *>>)-> Deferred<List<DataModel>>)? = null
    ){
       // lastResult = if (conditions != null) hostDto.select(conditions) else hostDto.select()
        if (block != null) {
            this.block(dtos())  // Continue execution if block is provided
        } else {
            checkout(lastResult)  // Immediately return result if no block
        }
    }

    /**
     * Updates existing records in the database with the provided data models.
     *
     * If a lambda function (`block`) is provided, it processes the updated data inside a `SequenceContext`,
     * enabling further modifications or validations.
     * If no lambda is provided, the function immediately returns a `Deferred` result.
     *
     * @param dataModels The list of `DATA` models to be updated.
     * @param block Optional suspend lambda function executed within a `SequenceContext`,
     *              allowing additional processing of the updated DTOs.
     */
    suspend fun update(
        dataModels: List<DataModel>,
        block: (suspend SequenceContext2<DTO>.(dtos: List<CommonDTO<DTO, *, *>>)-> Deferred<List<DataModel>>)? = null
    ) {
        //lastResult = hostDto.update<DATA, ENTITY>(dataModels)
        if (block != null) {
            this.block(dtos())  // Continue execution if block is provided
        } else {
            checkout(lastResult)  // Immediately return result if no block
        }
    }

    /**
     * Picks a subset of data from the database based on the specified query conditions.
     *
     * Similar to `select`, but intended for cases where a refined subset of data is needed.
     * If a lambda function (`block`) is provided, it processes the selected data inside a `SequenceContext`.
     * If no lambda is provided, the function immediately returns a `Deferred` result.
     *
     * @param T The table type extending `IdTable<Long>`.
     * @param conditions Query conditions specifying which records to pick.
     * @param block Optional suspend lambda function executed within a `SequenceContext`,
     *              allowing further processing of the selected DTOs.
     */
    suspend fun <T: IdTable<Long>> pick(
        conditions: QueryConditions<T>,
        block: (suspend SequenceContext2<DTO>.(dtos: List<CommonDTO<DTO, DataModel, ExposifyEntityBase>>)-> Unit)? = null
    ) {
       // lastResult = hostDto.pick(conditions)

        if (block != null) {
            this.block(dtos())
        } else {
            checkout(lastResult)
        }
    }


}


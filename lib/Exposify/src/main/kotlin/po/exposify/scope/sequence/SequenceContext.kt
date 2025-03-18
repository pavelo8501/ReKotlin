package po.exposify.scope.sequence

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.models.CrudResult
import po.exposify.scope.dto.DTOContext
import po.exposify.scope.sequence.classes.SequenceHandler
import kotlin.reflect.KProperty1

class SequenceContext<DATA, ENTITY>(
    val connection: Database,
    val hostDto : DTOClass<DATA,ENTITY>,
    private val handler : SequenceHandler<DATA>
) where  DATA : DataModel, ENTITY : LongEntity
{

    private fun  <T>dbQuery(body : () -> T): T = transaction(connection) {
        body()
    }

    private var lastResult : CrudResult<DATA, ENTITY>? = null

    private fun dtos(): List<CommonDTO<DATA,ENTITY>>{
        val result =   mutableListOf<CommonDTO<DATA,ENTITY>>()
        lastResult?.rootDTOs?.forEach{  result.add(it) }
        return result
    }


    suspend fun checkout() {
        val newDtoContext = DTOContext<DATA, ENTITY>(
            CrudResult<DATA, ENTITY>(dtos(), null),
        )
        handler.submitResult(newDtoContext.getData())
    }

    fun <SWITCH_DATA: DataModel, SWITCH_ENTITY : LongEntity> DTOClass<SWITCH_DATA, SWITCH_ENTITY>.switch(
        block:  SequenceContext<SWITCH_DATA, SWITCH_ENTITY>.(dtos: List<CommonDTO<SWITCH_DATA, SWITCH_ENTITY>>)->Unit ){
        val list = dtos().map { it.getChildren<SWITCH_DATA, SWITCH_ENTITY>(this) }.flatten()
        val result =  CrudResult<SWITCH_DATA, SWITCH_ENTITY>(list, null)
        val newSequenceContext =  SequenceContext<SWITCH_DATA, SWITCH_ENTITY>(
            connection,
            this,
            handler as SequenceHandler<SWITCH_DATA>
        )
        newSequenceContext.block(list)
    }

    suspend fun select(
        block: suspend SequenceContext<DATA, ENTITY>.(dtos: List<CommonDTO<DATA, ENTITY>>)-> Unit
    ) {
        lastResult = hostDto.select()
        this.block(dtos())
    }



    suspend fun update(
        dataModels: List<DATA>,
        block: suspend (dtos: List<CommonDTO<DATA, ENTITY>>)-> Unit
    ) {
        lastResult = hostDto.update<DATA, ENTITY>(dataModels)
        block(dtos())
    }

    @JvmName("UpdateDtos")
    suspend fun List<CommonDTO<DATA,ENTITY>>.update(
        block: (dtos: List<CommonDTO<DATA,ENTITY>>)-> Unit
    ) {
        lastResult = hostDto.update<DATA, ENTITY>(this.map { it.getInjectedModel() })
        block(dtos())
    }

    /**
     * Dynamically fetches a list of DTOs from the database based on the provided conditions
     * and executes a block of code with the resulting DTOs.
     *
     * This function is intended to be used within a `SequenceContext`, allowing sequence handlers
     * to retrieve and operate on filtered data dynamically.
     *
     * @param conditions A list of conditions, where each condition is a [Pair] consisting of:
     *   - A property of [DATA], defined as [KProperty1], used to filter the data.
     *   - A value of type [Any?], which represents the expected value for the corresponding property.
     * @param block A lambda block that is executed with the fetched list of [CommonDTO]s.
     * The block provides access to the filtered DTOs for further operations.
     *
     * ### How It Works
     * 1. Checks if the sequence handler contains any input data using `handler.inputData`.
     * 2. If input data exists, fetches the filtered results using the specified conditions.
     * 3. Lazily initializes the results by querying the database and invoking the block with the fetched DTOs.
     *
     * ### Usage Example
     *
     * ```kotlin
     * sequenceContext.pick(
     *     listOf(
     *         PartnerDataModel::name to "John Doe",
     *         PartnerDataModel::isActive to true
     *     )
     * ) { dtos ->
     *     // Perform actions with the fetched DTOs
     *     println("Fetched DTOs: $dtos")
     * }
     * ```
     *
     * The above example will:
     * 1. Build a query to fetch DTOs where `name == "John Doe"` and `isActive == true`.
     * 2. Execute the block with the resulting DTOs, if any are found.
     *
     * ### Notes
     * - The function uses `dbQuery` internally to query the database.
     * - The block will only execute if `handler.inputData` contains at least one entry.
     * - If no data matches the conditions, the block will not be executed.
     */
    suspend fun pick(
        vararg conditions: Pair<KProperty1<DATA, *>, Any?>,
        block: suspend (dtos: List<CommonDTO<DATA, ENTITY>>)-> Unit
    ) {
        handler.inputData?.firstOrNull()?.let {
            lastResult = hostDto.pick(conditions.toList())
            block(dtos())
        }
    }

    /**
     * Select with conditions
     */
    suspend fun select(
        conditions: List<Pair<KProperty1<DATA, *>, Any?>>,
        block: suspend SequenceContext<DATA, ENTITY>.(dtos: List<CommonDTO<DATA, ENTITY>>)-> Unit
    ) {
        lastResult = hostDto.select(conditions)
        this.block(dtos())
    }

}


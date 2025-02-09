package po.exposify.scope.service

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import po.exposify.classes.DTOClass
import po.exposify.scope.dto.DTOContext
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.scope.sequence.SequenceContext
import po.exposify.scope.sequence.classes.DefaultSequenceHandler
import po.exposify.scope.sequence.classes.SequenceHandler
import po.exposify.scope.sequence.models.SequencePack
import po.exposify.scope.service.enums.WriteMode
import kotlin.reflect.KProperty1

class ServiceContext<DATA,ENTITY>(
    private val dbConnection: Database,
    internal val rootDtoModel : DTOClass<DATA,ENTITY>,
) where  ENTITY : LongEntity,DATA: DataModel{

    val name : String = "${rootDtoModel.className}|Service"

    internal val sequences =
        mutableMapOf<SequenceHandler<DATA>, SequencePack<DATA, ENTITY>>()

    private fun  <T>dbQuery(body : () -> T): T = transaction(dbConnection) {
        body()
    }

    private fun <T> service(statement: ServiceContext<DATA, ENTITY>.() -> T): T = statement.invoke(this)
    fun <T> context(serviceBody: ServiceContext<DATA, ENTITY>.() -> T): T = service{
        serviceBody()
    }

    /**
     * Dynamically selects DTOs from the database based on the provided conditions and executes a block
     * of code within the context of the selected DTOs.
     *
     * @param conditions A vararg list of conditions, where each condition is a [Pair] consisting of:
     *   - A property of [DATA], defined as [KProperty1], to be used in the query.
     *   - A value of type [Any?], which represents the expected value for the corresponding property.
     * @param block A lambda block that operates on the selected DTOs, provided in the context of [DTOContext].
     * The block allows you to work with the fetched DTOs and perform additional actions.
     *
     * @return The same [DTOClass] instance or `null` if no DTOs are selected. The return value is primarily
     * intended for chaining purposes but can be ignored if not needed.
     *
     * ### Usage Example
     *
     * ```kotlin
     * PartnerDTO.pick(
     *     PartnerDataModel::name to "John Doe",
     *     PartnerDataModel::isActive to true
     * ) {
     *     // Code block executed with the selected DTOs
     *     println("Selected partners: $this")
     * }
     * ```
     *
     * The above example will:
     * 1. Dynamically build a query to select `PartnerDTO` entries where `name == "John Doe"` and `isActive == true`.
     * 2. Provide the selected DTOs in the context of the block.
     * 3. Allow you to operate on the selected DTOs inside the block (e.g., logging, modifying, etc.).
     *
     * ### Notes
     * - The function uses `dbQuery` internally to fetch the selected DTOs.
     * - If no DTOs match the conditions, the block will still execute with an empty context.
     */
    fun DTOClass<DATA, ENTITY>.pick(
        vararg conditions: Pair<KProperty1<DATA, *>, Any?>, block: DTOContext<DATA, ENTITY>.() -> Unit
    ): DTOClass<DATA, ENTITY>?
    {
        val selectedDTOs = dbQuery {
            runBlocking {
                pick(conditions.toList())
            }
        }
        val context  = DTOContext(selectedDTOs)
        context.block()
        return null
    }

    suspend fun DTOClass<DATA, ENTITY>.select(block: DTOContext<DATA, ENTITY>.() -> Unit){
        val selectedDTOs = dbQuery {
            runBlocking {
                select()
            }
        }
        val context  = DTOContext(selectedDTOs)
        context.block()
    }

    @JvmName("updateFromDataModels")
    fun DTOClass<DATA, ENTITY>.update(
        dataModels : List<DATA>,
        writeMode: WriteMode = WriteMode.STRICT,
        block: DTOContext<DATA, ENTITY>.() -> Unit){
        val createdDTOs =  dbQuery {
            runBlocking {
                update<DATA, ENTITY>(dataModels)
            }
        }
        val context = DTOContext(createdDTOs)
        context.block()
    }

    fun DTOClass<DATA, ENTITY>.update(
        dtoList : List<CommonDTO<DATA, ENTITY>>,
        block: DTOClass<DATA, ENTITY>.() -> Unit
    ){
        TODO("To implement update variance if DTOFunctions list is supplied")
    }

    fun DTOClass<DATA, ENTITY>.delete(toDelete: DATA, block: DTOContext<DATA, ENTITY>.() -> Unit){
        val selectedDTOs = dbQuery {
            runBlocking {
                delete(toDelete)
            }
        }
        val context  = DTOContext(selectedDTOs)
        context.block()
    }

    fun DTOClass<DATA, ENTITY>.sequence(
        name:String,
        block: suspend SequenceContext<DATA, ENTITY>.(List<DATA>?) -> Unit
    ) {
        val defaultHandler = DefaultSequenceHandler<DATA>(rootDtoModel, name)
        val container = SequencePack(
            SequenceContext<DATA, ENTITY>(dbConnection, rootDtoModel, defaultHandler),
            block,
            defaultHandler
        )
        sequences[defaultHandler] = container
    }

    fun DTOClass<DATA, ENTITY>.sequence(
        handler: SequenceHandler<DATA>,
        block: suspend SequenceContext<DATA, ENTITY>.(List<DATA>?) -> Unit
    ) {
        val container = SequencePack(
            SequenceContext<DATA, ENTITY>(dbConnection, rootDtoModel, handler),
            block,
            handler
        )
        sequences[handler] = container
    }

}
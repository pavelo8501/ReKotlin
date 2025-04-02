package po.exposify.scope.service

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import po.exposify.classes.interfaces.DataModel
import po.exposify.common.interfaces.AsContext
import po.exposify.common.models.CrudResult2
import po.exposify.classes.DTOClass
import po.exposify.dto.extensions.delete
import po.exposify.dto.extensions.pick
import po.exposify.dto.extensions.select
import po.exposify.dto.extensions.update
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.extensions.QueryConditions
import po.exposify.extensions.WhereCondition
import po.exposify.scope.dto.DTOContext
import po.exposify.scope.sequence.SequenceContext2
import po.exposify.scope.sequence.classes.SequenceHandler2
import po.exposify.scope.sequence.models.SequencePack2
import kotlin.reflect.KProperty1

class ServiceContext<DTO, DATA>(
    private  val serviceClass : ServiceClass<DTO, DATA, *>,
    internal val rootDtoModel : DTOClass<DTO>,
): AsContext<DATA>  where DTO : ModelDTO, DATA: DataModel{

    private val dbConnection: Database = serviceClass.connection
    val name : String = "${rootDtoModel.personalName}|Service"

    init {
        rootDtoModel.asHierarchyRoot(this)
    }

    internal fun  <T>dbQuery(body : () -> T): T = transaction(dbConnection) {
        body()
    }

    //private fun <T> service(statement: ServiceContext2<DTO, DATA>.() -> T): T = statement.invoke(this)

//    fun <T> context(serviceBody: ServiceContext2<DTO, DATA>.() -> T): T = service{
//        serviceBody()
//    }

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
    fun <T: IdTable<Long>>pick(conditions: QueryConditions<T>): Deferred<CrudResult2<DTO>>{
        val crudResult = dbQuery {
            runBlocking {
                rootDtoModel.pick<DTO, DATA, ExposifyEntityBase, T>(conditions)
            }
        }
        return CompletableDeferred<CrudResult2<DTO>>(crudResult)
    }

    fun select(): Deferred<CrudResult2<DTO>> {
        val crudResult = dbQuery {
            runBlocking {
                rootDtoModel.select<DTO, DATA, ExposifyEntityBase>()
            }
        }
        return CompletableDeferred<CrudResult2<DTO>>(crudResult)
    }

    fun <T: IdTable<Long>> select(conditions : WhereCondition<T>): Deferred<CrudResult2<DTO>>{
        val crudResult = dbQuery {
            runBlocking {
                rootDtoModel.select<DTO, DATA, ExposifyEntityBase, T>(conditions)
            }
        }
        return CompletableDeferred<CrudResult2<DTO>>(crudResult)
    }

    fun update(dataModels : List<DATA>): Deferred<CrudResult2<DTO>>  {
        val crudResult = dbQuery {
            runBlocking {
                rootDtoModel.update<DTO>(dataModels)
            }
        }
       return  CompletableDeferred<CrudResult2<DTO>>(crudResult)
    }

    fun delete(toDelete: DATA): Deferred<CrudResult2<DTO>>{
        val crudResult = dbQuery {
            runBlocking {
                rootDtoModel.delete<DTO, DATA, ExposifyEntityBase>(toDelete)
            }
        }
        return  CompletableDeferred<CrudResult2<DTO>>(crudResult)
    }

    fun sequence(
        handler: SequenceHandler2<DTO>,
        block: suspend SequenceContext2<DTO>.() -> Unit
    ) {
        val sequenceContext = SequenceContext2<DTO>(dbConnection, rootDtoModel, handler)
        handler.addSequence(SequencePack2(sequenceContext, serviceClass, block, handler))
    }

//    private val context : SequenceContext<DATA,ENTITY>,
//    private val serviceClass: ServiceClass<DATA, ENTITY>,
//    private val sequenceFn : suspend  SequenceContext<DATA, ENTITY>.() -> Deferred<List<DATA>>,
//    private val handler: SequenceHandler<DATA>,

}
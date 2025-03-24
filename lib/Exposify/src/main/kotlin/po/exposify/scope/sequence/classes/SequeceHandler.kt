package po.exposify.scope.sequence.classes

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.auth.AuthSessionManager
import po.auth.sessions.models.AuthorizedSession
import po.exposify.classes.DTOClass
import po.exposify.classes.components.safeCast
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.exceptions.ExceptionCodes
import po.exposify.exceptions.OperationsException
import po.exposify.scope.sequence.SequenceID
import po.exposify.scope.sequence.classes.execute
import po.exposify.scope.sequence.models.SequencePack
import po.exposify.scope.service.ServiceContext
import kotlin.reflect.full.companionObjectInstance


suspend inline fun <reified DTO> AuthorizedSession.execute(
    sequenceId:Int
): Unit  where DTO : CommonDTO<*, *>  {

    val dtos = sequenceOf(DTO::class).toList()
    dtos.first { it.companionObjectInstance ==   DTO::class.companionObjectInstance}.objectInstance!!.sourceModel.execute(sequenceId)
}


private suspend fun <DATA: DataModel,  ENTITY: LongEntity>  runExecute(
    session: AuthorizedSession,
    dtoClass: DTOClass<DATA, ENTITY>,
    sequenceId: String,
    params: Map<String, String>? = null,
    inputList: List<DATA>? = null): Deferred<List<DATA>>{

    val lookupKey = "sequence:${dtoClass.className}::$sequenceId"

    session.getSessionAttr<SequenceHandler<DATA, ENTITY>>(lookupKey)?.let { handler ->
        if (params != null && inputList != null) { return   handler.execute(params, inputList) }
        if (params != null) { return handler.execute(params) }
        if (inputList != null) { return handler.execute(inputList) }
        return handler.execute()
    }?: run {
        println("Session attribute with key : $lookupKey not found in registry")
        return CompletableDeferred<List<DATA>>(emptyList())
    }
}


suspend fun <DATA: DataModel,  ENTITY: LongEntity>  AuthorizedSession.execute(
    dtoClass: DTOClass<DATA, ENTITY>,
    sequenceID: SequenceID,
    params: Map<String, String>? = null,
    inputList: List<DATA>? = null): Deferred<List<DATA>> {
    return runExecute<DATA, ENTITY>(this, dtoClass, sequenceID.value.toString(), params, inputList)
}

suspend fun <DATA: DataModel,  ENTITY: LongEntity>  AuthorizedSession.execute(
    dtoClass: DTOClass<DATA, ENTITY>,
    sequenceId:Int,
    params: Map<String, String>? = null,
    inputList: List<DATA>? = null): Deferred<List<DATA>> {
    return runExecute<DATA, ENTITY>(this, dtoClass, sequenceId.toString(), params, inputList)
}

/**
* Creates a new [SequenceHandler] instance using a predefined [SequenceID] type.
*
* You can use the built-in [SequenceID] enum for common handler IDs, or define your own
* enum with an `Int`-based identifier for custom behavior:
*
* ```
* enum class MyHandlerType(val value: Int) {
    *     CREATE(0), ARCHIVE(1)
    * }
* ```
*
* Then pass `MyHandlerType.CREATE` into the overload that accepts an [Int].
*
* @param dto The DTO definition representing the current data model being processed.
* @param id A predefined [SequenceID] value describing the type of sequence handler to create.
*
* @return A newly created and session-scoped [SequenceHandler].
*/
suspend fun <DATA : DataModel, ENTITY : LongEntity> ServiceContext<DATA, ENTITY>.createHandler(
    dto: DTOClass<DATA, ENTITY>,
    sequenceID: SequenceID
): SequenceHandler<DATA, ENTITY> = this.createHandler(dto, sequenceID.value)

/**
 * Creates a new [SequenceHandler] instance for the given [dto] class and associates it
 * with the current [AuthorizedSession] using the provided [handleId].
 *
 * This handler will be stored in the session context and can be reused or referenced
 * by other sequence calls within the same session scope.
 *
 * @param dto The DTO definition representing the current data model being processed.
 * @param handleId An integer representing the unique identifier of the handler logic.
 *                 Can be a raw value or a mapped value from [SequenceID].
 *
 * @return A newly created [SequenceHandler] registered into the current session.
 *
 * @see SequenceID For common predefined handler types.
 */
suspend fun <DATA : DataModel, ENTITY : LongEntity> ServiceContext<DATA, ENTITY>.createHandler(
    dto: DTOClass<DATA, ENTITY>,
    handleId: Int):SequenceHandler<DATA, ENTITY> {
    val session = AuthSessionManager.getCurrentSession()
    val newHandler =  SequenceHandler<DATA, ENTITY>(dto, handleId)
    val handlerKey = "sequence:${newHandler.thisKey}"
    session.setSessionAttr(handlerKey, newHandler)
    return  newHandler
}

/**
 * Represents a sealed interface for handling sequence execution and result callbacks.
 * @param T The type of data processed by the sequence handler.
 */
sealed interface SequenceHandlerInterface<DATA: DataModel, ENTITY: LongEntity> {

    val dtoClass : DTOClass<DATA, ENTITY>
    /**
     * The unique name of the sequence handler.
     */
    val thisKey: String

    /**
     * Invokes the result callback function with the provided data.
     * @param listedData The data to be passed to the result callback.
     */
   // suspend fun submitResult(result: List<DATA>)
}

/**
 * An abstract class representing a sequence handler, responsible for managing
 * the execution of sequences and handling their result callbacks.
 *
 * @param T The type of data processed by the sequence handler. Must be a list of [DataModel].
 * @property name The unique name of the sequence handler.
 * @property resultCallback An optional function that is invoked when a sequence result is available.
 */
abstract class SequenceHandlerAbstraction<DATA, ENTITY>(
    override val dtoClass: DTOClass<DATA, ENTITY>,
) : SequenceHandlerInterface<DATA, ENTITY> where  DATA : DataModel, ENTITY: LongEntity  {

    abstract override val thisKey: String

    private val sequences =
        mutableMapOf<String, SequencePack<DATA, ENTITY>>()

    internal fun getStockSequence(key: String = "0"): SequencePack<DATA, ENTITY>{
        val lookupKey =   "${dtoClass.className}::$key"
        val sequence = sequences[thisKey]
        if(sequence != null){
            return  sequence
        }else{
            throw OperationsException("Unable to find sequence for a given handler $lookupKey", ExceptionCodes.KEY_NOT_FOUND )
        }
    }

    fun addSequence(sequence: SequencePack<DATA, ENTITY>){
        sequences[thisKey] = sequence
    }

    suspend fun execute(params: Map<String, String>, key: String = "0"): Deferred<List<DATA>>{
        getStockSequence(key).let {
            it.saveParams(params)
            it.saveInputList(emptyList())
            return it.serviceClass.launchSequence(it)
        }
    }

    suspend fun execute(inputList : List<DATA>, key: String = "0"): Deferred<List<DATA>> {
        getStockSequence(key).let {
            it.saveInputList(inputList)
            it.saveParams(emptyMap())
            return it.serviceClass.launchSequence(it)
        }
    }

    suspend fun execute(params: Map<String, String>, inputList : List<DATA>, key: String = "0"): Deferred<List<DATA>>{
        getStockSequence(key).let {
            it.saveParams(params)
            it.saveInputList(inputList)
            return it.serviceClass.launchSequence(it)
        }
    }
    suspend fun execute(key: String = "0"): Deferred<List<DATA>>{
        getStockSequence(key).let {
            it.saveParams(emptyMap())
            it.saveInputList(emptyList())
            return it.serviceClass.launchSequence(it)
        }
    }
}

class SequenceHandler<DATA, ENTITY>(
    override val dtoClass: DTOClass<DATA, ENTITY>,
    val handlerId: Int,
): SequenceHandlerAbstraction<DATA, ENTITY>(dtoClass) where  DATA : DataModel, ENTITY: LongEntity{

    override val thisKey: String = "${dtoClass.className}::${handlerId.toString()}"

    init {
        val a = thisKey
    }

}
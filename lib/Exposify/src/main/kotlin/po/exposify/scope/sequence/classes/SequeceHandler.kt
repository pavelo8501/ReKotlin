package po.exposify.scope.sequence.classes

import kotlinx.coroutines.Deferred
import org.jetbrains.exposed.dao.LongEntity
import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.DataModel
import po.exposify.exceptions.ExceptionCodes
import po.exposify.exceptions.OperationsException
import po.exposify.scope.sequence.models.SequencePack
import po.exposify.scope.session.interfaces.UserSession

inline fun <reified DATA, reified ENTITY> UserSession.sequenceHandler(
    dto: DTOClass<DATA, ENTITY>
): SequenceHandler<DATA, ENTITY> where DATA: DataModel, ENTITY : LongEntity   {
    val handlerSessionKey = "Handler::${dto.className} ::$sessionId"

    return getAttribute<SequenceHandler<DATA, ENTITY>>(handlerSessionKey)
        ?: object : SequenceHandler<DATA, ENTITY>(dto,handlerSessionKey) {}
            .also { setAttribute(handlerSessionKey, it) }
}

//suspend fun <DATA : DataModel, ENTITY : LongEntity> ServiceContext<DATA, ENTITY>.createHandler(dto: DTOClass<DATA, ENTITY>):SequenceHandler<DATA, ENTITY> {
//    CoroutineSessionHolder.createSessionContext(userId = 1)
//     val session = CoroutineSessionHolder.createSessionContext(1)
//    val handlerSessionKey = "Handler::${dto.className} ::${session.sessionId}"
//    val newHandler = object : SequenceHandler<DATA, ENTITY>(dto, handlerSessionKey){}
//    session.setAttribute(handlerSessionKey, newHandler)
//    return  newHandler
//}
/**
 * Represents a sealed interface for handling sequence execution and result callbacks.
 * @param T The type of data processed by the sequence handler.
 */
sealed interface SequenceHandlerInterface<DATA: DataModel, ENTITY: LongEntity> {

    val dtoClass : DTOClass<DATA, ENTITY>
    /**
     * The unique name of the sequence handler.
     */
    val name: String

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
abstract class SequenceHandler<DATA, ENTITY>(
    override val dtoClass: DTOClass<DATA, ENTITY>,
    override val name: String,
) : SequenceHandlerInterface<DATA, ENTITY> where  DATA : DataModel, ENTITY: LongEntity  {


    internal val sequences =
        mutableMapOf<String, SequencePack<DATA, ENTITY>>()

    internal fun getStockSequence(): SequencePack<DATA, ENTITY>{
        val sequence = sequences[name]
        if(sequence != null){
            return  sequence
        }else{
            throw OperationsException("Unable to find sequence for a given handler ${this.name}", ExceptionCodes.KEY_NOT_FOUND )
        }
    }

    suspend fun execute(params: Map<String, String> ): Deferred<List<DATA>>{
        getStockSequence().let {
            it.saveParams(params)
            it.saveInputList(emptyList())
            return it.serviceClass.launchSequence(it)

        }
    }

    suspend fun execute(inputList : List<DATA>): Deferred<List<DATA>> {
        getStockSequence().let {
            it.saveInputList(inputList)
            it.saveParams(emptyMap())
            return it.serviceClass.launchSequence(it)
        }
    }

    suspend fun execute(params: Map<String, String>, inputList : List<DATA>): Deferred<List<DATA>>{
        getStockSequence().let {
            it.saveParams(params)
            it.saveInputList(inputList)
            return it.serviceClass.launchSequence(it)
        }
    }
    suspend fun execute(): Deferred<List<DATA>>{
        getStockSequence().let {
            it.saveParams(emptyMap())
            it.saveInputList(emptyList())
            return it.serviceClass.launchSequence(it)
        }
    }

}
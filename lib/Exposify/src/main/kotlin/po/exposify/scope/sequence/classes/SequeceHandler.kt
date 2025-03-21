package po.exposify.scope.sequence.classes

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Op
import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.exceptions.ExceptionCodes
import po.exposify.exceptions.OperationsException
import po.exposify.scope.dto.DTOContext
import po.exposify.scope.sequence.models.SequencePack
import kotlin.reflect.KProperty1

/**
 * Represents a sealed interface for handling sequence execution and result callbacks.
 * @param T The type of data processed by the sequence handler.
 */
sealed interface SequenceHandlerInterface<T: DataModel> {
    val dtoClass : DTOClass<T, *>
    /**
     * The unique name of the sequence handler.
     */
    val name: String

    /**
     * Invokes the result callback function with the provided data.
     * @param listedData The data to be passed to the result callback.
     */
    suspend fun submitResult(result: List<T>)
}

/**
 * An abstract class representing a sequence handler, responsible for managing
 * the execution of sequences and handling their result callbacks.
 *
 * @param T The type of data processed by the sequence handler. Must be a list of [DataModel].
 * @property name The unique name of the sequence handler.
 * @property resultCallback An optional function that is invoked when a sequence result is available.
 */
abstract class SequenceHandler<T>(
    override val dtoClass: DTOClass<T, *>,
    override val name: String,
    private var resultCallback: (suspend (List<T>) -> Unit)? = null
) : SequenceHandlerInterface<T> where  T: DataModel {

    /**
     * Stores the input data associated with the current sequence execution.
     */
    internal var inputData  : List<T>? = null

    internal val sequences =
        mutableMapOf<String, SequencePack<T, *>>()


    internal fun getStockSequence(): SequencePack<T,*>{
        val sequence = sequences[name]
        if(sequence != null){
            return  sequence
        }else{
            throw OperationsException("Unable to find sequence for a given handler ${this.name}", ExceptionCodes.KEY_NOT_FOUND )
        }
    }

    suspend fun execute(params: Map<String, String> ): Deferred<List<T>>{
        getStockSequence().let {
            it.saveParams(params)
            it.saveInputList(emptyList())
            return  dtoClass.emitter.launchSequence(it)
        }
    }

    suspend fun execute(inputList : List<T>): Deferred<List<T>> {
        getStockSequence().let {
            it.saveInputList(inputList)
            it.saveParams(emptyMap())
            return dtoClass.emitter.launchSequence(it)
        }
    }

    suspend fun execute(params: Map<String, String>, inputList : List<T>): Deferred<List<T>>{
        getStockSequence().let {
            it.saveParams(params)
            it.saveInputList(inputList)
            return dtoClass.emitter.launchSequence(it)
        }
    }
    suspend fun execute(): Deferred<List<T>>{
        getStockSequence().let {
            it.saveParams(emptyMap())
            it.saveInputList(emptyList())
            return dtoClass.emitter.launchSequence(it)
        }
    }

    private var onResult :((List<T>)-> Unit) ? = null
    fun onResultSubmitted(callback : (List<T>)-> Unit){
        onResult =  callback
    }

    /**
     * Invokes the result callback function with the provided data.
     * @param listedData The data to pass to the result callback.
     */
    override suspend fun submitResult(result : List<T> ){
        onResult?.invoke(result)
    }

}
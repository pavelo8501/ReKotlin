package po.exposify.scope.sequence.classes

import kotlinx.coroutines.Deferred
import org.jetbrains.exposed.dao.LongEntity
import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO

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
    suspend fun invokeResultCallback(listedData: List<T>)
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

    /**
     * Indicates whether input data has been assigned.
     * @return `true` if input data is available, `false` otherwise.
     */
    val hasInputData : Boolean
        get() { return inputData != null }

    /**
     * Indicates whether a result callback function is assigned.
     * @return `true` if a callback is assigned, `false` otherwise.
     */
    val hasResultCallback : Boolean
        get() { return resultCallback != null }

//    /**
//     * Executes the stored result callback function with the provided data.
//     * @param listedData The data to pass to the result callback.
//     */
//    internal fun executeCallback(listedData : T){
//       // resultCallback?.invoke(listedData)
//    }

    internal fun getResultCallback():(suspend (List<T>) -> Unit)?{
        return resultCallback
    }

    fun getData(): List<T>{
        return this.inputData?:emptyList()
    }

    fun getFrst(): T?{
        return this.inputData?.firstOrNull()
    }

    /**
     * Assigns a result callback function to be executed when the sequence completes.
     * @param callback The callback function to assign.
     */
    suspend  fun execute(data : List<T> = emptyList<T>(),  callback:suspend (List<T>)-> Unit){
        resultCallback = callback
        dtoClass.triggerSequence(this, data)
    }

    suspend  fun execute(data : List<T> = emptyList<T>()): Deferred<List<T>>{
        return  dtoClass.triggerSequence(this, data)
    }

    /**
     * Invokes the result callback function with the provided data.
     * @param listedData The data to pass to the result callback.
     */
    override suspend fun invokeResultCallback(listedData: List<T>) {
        onResult?.invoke(listedData)
    }

    private var onResult :  ((List<T>)-> Unit) ? = null
    fun onResultSubmitted(callback : (List<T>)-> Unit){
        onResult =  callback
    }

}
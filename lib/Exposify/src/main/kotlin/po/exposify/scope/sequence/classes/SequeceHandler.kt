package po.exposify.scope.sequence.classes

import po.exposify.classes.interfaces.DataModel

/**
 * Represents a sealed interface for handling sequence execution and result callbacks.
 * @param T The type of data processed by the sequence handler.
 */
sealed interface SequenceHandlerInterface<T> {
    /**
     * The unique name of the sequence handler.
     */
    val name: String

    /**
     * Invokes the result callback function with the provided data.
     * @param listedData The data to be passed to the result callback.
     */
    fun invokeResultCallback(listedData: T)
}

/**
 * An abstract class representing a sequence handler, responsible for managing
 * the execution of sequences and handling their result callbacks.
 *
 * @param T The type of data processed by the sequence handler. Must be a list of [DataModel].
 * @property name The unique name of the sequence handler.
 * @property resultCallback An optional function that is invoked when a sequence result is available.
 */
abstract class SequenceHandler<T : List<DataModel>>(
    override val name: String,
    private var resultCallback: ((T) -> Unit)? = null
) : SequenceHandlerInterface<T> {

    /**
     * Stores the input data associated with the current sequence execution.
     */
    internal var inputData  : T? = null

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

    /**
     * Executes the stored result callback function with the provided data.
     * @param listedData The data to pass to the result callback.
     */
    internal fun executeCallback(listedData : T){
        resultCallback?.invoke(listedData)
    }

    /**
     * Executes the sequence with the given input data and assigns a result callback function.
     * @param listedData The input data for the sequence execution.
     * @param callback The callback function to invoke when the sequence completes.
     */
    fun execute(listedData: T, callback:(T)-> Unit){
        inputData = listedData
        resultCallback = callback
    }

    /**
     * Assigns a result callback function to be executed when the sequence completes.
     * @param callback The callback function to assign.
     */
    fun execute(callback:(T)-> Unit){
        resultCallback = callback
    }

    /**
     * Invokes the result callback function with the provided data.
     * @param listedData The data to pass to the result callback.
     */
    override fun invokeResultCallback(listedData: T) {
        resultCallback?.invoke(listedData)
    }

}
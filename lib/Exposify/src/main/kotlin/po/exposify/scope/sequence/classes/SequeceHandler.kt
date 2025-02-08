package po.exposify.scope.sequence.classes

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
    fun invokeResultCallback(listedData: List<T>)
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
    private var resultCallback: ((List<T>) -> Unit)? = null
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

    internal fun getResultCallback():((List<T>) -> Unit)?{
        return resultCallback
    }

    fun getData(): List<T>{
        return this.inputData?:emptyList()
    }

    fun getFrst(): T?{
        return this.inputData?.firstOrNull()
    }

    /**
     * Executes the sequence with the given input data and assigns a result callback function.
     * @param listedData The input data for the sequence execution.
     * @param callback The callback function to invoke when the sequence completes.
     */
    fun execute(listedData: List<T>, callback:(List<T>)-> Unit){
        inputData = listedData
        resultCallback = callback
        dtoClass.triggerSequence(name)
    }

    /**
     * Assigns a result callback function to be executed when the sequence completes.
     * @param callback The callback function to assign.
     */
    fun execute(callback:(List<T>)-> Unit){
        resultCallback = callback
        dtoClass.triggerSequence(name)
    }

    /**
     * Invokes the result callback function with the provided data.
     * @param listedData The data to pass to the result callback.
     */
    override fun invokeResultCallback(listedData: List<T>) {
        resultCallback?.invoke(listedData)
    }

}
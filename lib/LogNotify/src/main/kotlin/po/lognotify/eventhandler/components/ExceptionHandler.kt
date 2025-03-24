package po.lognotify.eventhandler.components

import po.lognotify.eventhandler.exceptions.CancelException
import po.lognotify.eventhandler.exceptions.ProcessableException
import po.lognotify.eventhandler.exceptions.PropagateException
import po.lognotify.eventhandler.exceptions.SkipException
import po.lognotify.eventhandler.exceptions.UnmanagedException
import po.lognotify.shared.enums.HandleType
import java.io.IOException


interface ExceptionHandlerInterface{

    fun <E : ProcessableException>  registerSkipException(constructorFn : (defaultMessage: String) -> E)
    fun throwSkipException(msg: String):ProcessableException

    fun <E : ProcessableException> registerCancelException(
        cancelFn: () -> Unit, constructFn : (defaultMessage: String) ->E)
    fun throwCancelException(msg: String, cancelFn: (()->Unit)?):ProcessableException

    fun <E : ProcessableException> registerPropagateException(constructorFn : (defaultMessage: String) -> E)
    fun throwPropagateException(msg: String):ProcessableException

}

/**
 * A utility class for managing and raising specific types of `ProcessableException`.
 * This class implements the `ExceptionHandlerInterface` and provides default behavior
 * for handling exceptions related to `HandleType` (SKIP_SELF, CANCEL_ALL, PROPAGATE_TO_PARENT).
 *
 * By default, each exception type has a pre-configured constructor, but custom constructors
 * can be registered using the `register*Exception` methods.
 */
open class ExceptionHandler: ExceptionHandlerInterface{

    /**
     * Constructor function for creating `SkipException` instances.
     * By default, it generates a `SkipException` with the message "Default skip message".
     */
    private var skipExceptionConstructorFn : ((defaultMessage: String)-> ProcessableException) =
        { SkipException(it) }

    /**
     * Constructor function for creating `CancelException` instances.
     * By default, it generates a `CancelException` with the message "Default cancel message".
     */
    private var cancelExceptionConstructorFn :
            ((defaultMessage: String) -> ProcessableException) = { message->
            CancelException(message)
        }

    /**
     * Constructor function for creating `PropagateException` instances.
     * By default, it generates a `PropagateException` with the message "Default propagate message".
     */
    private var propagateExceptionConstructorFn : ((defaultMessage: String)->ProcessableException) =
        { PropagateException(it) }


    /**
     * Registers a custom constructor function for `SkipException`.
     *
     * @param exConstructFn A function that returns an instance of a `ProcessableException`
     *                      (or a subclass) with `HandleType.SKIP_SELF`.
     */

    private var cancelationFn : (() -> Unit)?  = null
    /**
     * Registers a custom constructor function for `CancelException`.
     *
     * @param exConstructFn A function that returns an instance of a `ProcessableException`
     *                      (or a subclass) with `HandleType.CANCEL_ALL`.
     */
    override fun <E: ProcessableException> registerCancelException(
        cancelFn: () -> Unit, constructFn : (defaultMessage: String) ->E
    ){
        cancelExceptionConstructorFn = constructFn
        cancelationFn = cancelFn
    }

    /**
     * Registers a custom constructor function for `PropagateException`.
     *
     * @param exConstructFn A function that returns an instance of a `ProcessableException`
     *                      (or a subclass) with `HandleType.PROPAGATE_TO_PARENT`.
     */
    override fun <E: ProcessableException> registerPropagateException(constructorFn : (defaultMessage: String) -> E){
        propagateExceptionConstructorFn = constructorFn
    }


    override fun <E : ProcessableException> registerSkipException(constructorFn : (defaultMessage: String) -> E){
        this.skipExceptionConstructorFn = constructorFn
    }

    /**
     * Raises a `SkipException` with the provided message.
     *
     * @param msg An optional message to override the default or registered exception's message.
     *            If null, the message from the registered constructor will be used.
     * @throws SkipException The exception to indicate that the current process should skip itself.
     */
    override fun throwSkipException(msg: String): ProcessableException{
        val skipException = skipExceptionConstructorFn.invoke(msg)
        skipException.handleType = HandleType.SKIP_SELF
        skipException.message = msg
        throw skipException
        return skipException
    }

    /**
     * Raises a `CancelException` with the provided message and invokes a custom cancellation function.
     *
     * @param msg An optional message to override the default or registered exception's message.
     *            If null, the message from the registered constructor will be used.
     * @param cancelFn A lambda function to execute custom cancellation logic when the exception is raised.
     *
     * @throws CancelException The exception to indicate that all related processes should be canceled.
     *         The exception will also execute the `cancelFn` to perform the specified cancellation actions.
     *
     * Example usage:
     * ```kotlin
     * raiseCancelException("Cancel operation") {
     *     println("Custom cancellation logic executed.")
     * }
     * ```
     */
    override fun throwCancelException(msg: String, cancelFn: (()->Unit)?):ProcessableException{
        val cancelException =  cancelExceptionConstructorFn.invoke(msg)
        cancelException.handleType = HandleType.CANCEL_ALL
        cancelException.message = msg
        cancelFn?.let {
            it.invoke()
        }?:run {
            cancelationFn!!.invoke()
        }

        throw cancelException
        return cancelException
    }

    /**
     * Raises a `PropagateException` with the provided message.
     *
     * @param msg An optional message to override the default or registered exception's message.
     *            If null, the message from the registered constructor will be used.
     * @throws PropagateException The exception to indicate that the exception should be propagated to the parent.
     */
    override fun throwPropagateException(msg: String):ProcessableException{
        val propagateException = propagateExceptionConstructorFn.invoke(msg)
        propagateException.handleType = HandleType.PROPAGATE_TO_PARENT
        propagateException.message = msg
        throw propagateException
        return propagateException
    }

}
package po.lognotify.eventhandler.components

import po.lognotify.eventhandler.exceptions.CancelException
import po.lognotify.eventhandler.exceptions.ProcessableException
import po.lognotify.eventhandler.exceptions.PropagateException
import po.lognotify.eventhandler.exceptions.SkipException
import po.lognotify.shared.enums.HandleType
import java.io.IOException

interface ExceptionHandlerInterface{
    fun <E : ProcessableException> registerSkipException(exConstructFn: () -> E)
    fun <E : ProcessableException> registerCancelException(exConstructFn: () -> E)
    fun <E : ProcessableException> registerPropagateException(exConstructFn: () -> E)
    fun raiseSkipException(msg: String? = null):ProcessableException
    fun raiseCancelException(msg: String? = null, cancelFn: () -> Unit)
    fun <E : ProcessableException> raisePropagateException(msg: String?, block : (E.()->Unit)? = null )
}

/**
 * A utility class for managing and raising specific types of `ProcessableException`.
 * This class implements the `ExceptionHandlerInterface` and provides default behavior
 * for handling exceptions related to `HandleType` (SKIP_SELF, CANCEL_ALL, PROPAGATE_TO_PARENT).
 *
 * By default, each exception type has a pre-configured constructor, but custom constructors
 * can be registered using the `register*Exception` methods.
 */
class ExceptionHandler : ExceptionHandlerInterface{

    /**
     * Constructor function for creating `SkipException` instances.
     * By default, it generates a `SkipException` with the message "Default skip message".
     */
    private var skipExceptionConstructorFn : (()->ProcessableException) =
        { SkipException("Default skip message").apply { handleType = HandleType.SKIP_SELF } }

    /**
     * Constructor function for creating `CancelException` instances.
     * By default, it generates a `CancelException` with the message "Default cancel message".
     */
    private var cancelExceptionConstructorFn : (()->ProcessableException) =
        { CancelException("Default skip message").apply { handleType = HandleType.SKIP_SELF } }

    /**
     * Constructor function for creating `PropagateException` instances.
     * By default, it generates a `PropagateException` with the message "Default propagate message".
     */
    private var propagateExceptionConstructorFn : (()->ProcessableException) =
        { PropagateException("Default skip message").apply { handleType = HandleType.PROPAGATE_TO_PARENT} }

    /**
     * Registers a custom constructor function for `SkipException`.
     *
     * @param exConstructFn A function that returns an instance of a `ProcessableException`
     *                      (or a subclass) with `HandleType.SKIP_SELF`.
     */
    override fun <E: ProcessableException> registerSkipException(exConstructFn : ()->E){
        skipExceptionConstructorFn = exConstructFn
    }

    /**
     * Registers a custom constructor function for `CancelException`.
     *
     * @param exConstructFn A function that returns an instance of a `ProcessableException`
     *                      (or a subclass) with `HandleType.CANCEL_ALL`.
     */
    override fun <E: ProcessableException> registerCancelException(exConstructFn : ()->E){
        cancelExceptionConstructorFn = exConstructFn
    }

    /**
     * Registers a custom constructor function for `PropagateException`.
     *
     * @param exConstructFn A function that returns an instance of a `ProcessableException`
     *                      (or a subclass) with `HandleType.PROPAGATE_TO_PARENT`.
     */
    override fun <E: ProcessableException> registerPropagateException(exConstructFn : ()->E){
        propagateExceptionConstructorFn = exConstructFn
    }

    /**
     * Raises a `SkipException` with the provided message.
     *
     * @param msg An optional message to override the default or registered exception's message.
     *            If null, the message from the registered constructor will be used.
     * @throws SkipException The exception to indicate that the current process should skip itself.
     */
    override fun raiseSkipException(msg: String?): ProcessableException{
        val skipException = skipExceptionConstructorFn.invoke()
        skipException.handleType = HandleType.SKIP_SELF
        skipException.message = msg?:skipException.message
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
    override fun raiseCancelException(msg: String?, cancelFn: ()->Unit){
        val cancelException = cancelExceptionConstructorFn.invoke()
        cancelException.handleType = HandleType.CANCEL_ALL
        cancelException.message = msg?:cancelException.message
        cancelException.cancellationFn = cancelFn
        throw cancelException
    }

    /**
     * Raises a `PropagateException` with the provided message.
     *
     * @param msg An optional message to override the default or registered exception's message.
     *            If null, the message from the registered constructor will be used.
     * @throws PropagateException The exception to indicate that the exception should be propagated to the parent.
     */
    override fun <E : ProcessableException> raisePropagateException(msg: String?, block : (E.()->Unit)?){
        val propagateException = propagateExceptionConstructorFn.invoke()
        propagateException.handleType = HandleType.PROPAGATE_TO_PARENT
        propagateException.message = msg?:propagateException.message
        if(block!=null){
            try {
                @Suppress("UNCHECKED_CAST")
                (propagateException as E).block()
            }catch(ex: IOException){
                println("Convenience method raisePropagateException thrown an exception ${ex.message.toString()}")
            }
        }
        throw propagateException
    }

}
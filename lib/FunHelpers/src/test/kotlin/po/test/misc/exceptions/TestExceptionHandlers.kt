package po.test.misc.exceptions

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.coroutines.CoroutineInfo
import po.misc.data.helpers.output
import po.misc.data.logging.ContextAware
import po.misc.exceptions.trackable.TrackableException
import po.misc.exceptions.handling.Suspended
import po.misc.exceptions.handling.delegateIfThrow
import po.misc.exceptions.handling.registerHandler
import po.misc.exceptions.metaFrameTrace
import po.misc.exceptions.stack_trace.ExceptionTrace
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class TestExceptionHandlers() : ContextAware {


    class SimpleTrackableException(val context: Any, message: String): Throwable(message), TrackableException{
        override val contextClass: KClass<*> get() = context::class
        override val exceptionTrace: ExceptionTrace = metaFrameTrace(contextClass)
        override val self: SimpleTrackableException = this
        override var coroutineInfo: CoroutineInfo? = null
    }

    class ScopedException(val context: Any, message: String): Throwable(message), TrackableException{
        override val contextClass: KClass<*> get() = context::class
        override val exceptionTrace: ExceptionTrace = metaFrameTrace(context::class, 5)
        override val self: ScopedException = this
        override var coroutineInfo: CoroutineInfo? = null

    }

    class ObjectDelegating(val channel : TestExceptionHandlers){
        fun <TH: Throwable> throwingBlock(exception: TH){
            channel.delegateIfThrow<String> {
                throw exception
            }
        }
        suspend fun <TH: Throwable> suspendingThrowingBlock(exception: TH){
            channel.delegateIfThrow(Suspended) {
                throw exception
            }
        }
    }

    @Test
    fun `Instantiation Functions`(){
        var registerHandlerLambda: Any? = null


        registerHandler<SimpleTrackableException> { exception->
            exception.output()
            throw exception

        }
        var handledBySuspendableLambda: Any? = null
        registerHandler<SimpleTrackableException>(){exception->
            exception.output()
            throw exception
        }
        assertDoesNotThrow {
            val thrower = ObjectDelegating(this)
            thrower.throwingBlock(SimpleTrackableException(this, "Some"))
        }
        assertIs<SimpleTrackableException>(registerHandlerLambda).output()
        assertNull(handledBySuspendableLambda)
    }

    @Test
    fun `Instantiation Functions for suspended`() = runTest{

        var registerHandlerLambda: Any? = null
        registerHandler<SimpleTrackableException> { exception->
            exception.output()
            throw exception
        }
        var handledBySuspendableLambda: Any? = null
        registerHandler<SimpleTrackableException>(){exception->
            exception.output()
            throw exception
        }

        assertDoesNotThrow {
            val thrower = ObjectDelegating(this@TestExceptionHandlers)
            thrower.throwingBlock(SimpleTrackableException(this@TestExceptionHandlers, "Non suspending"))
        }

        assertDoesNotThrow {

            delegateIfThrow<SimpleTrackableException>(Suspended) {

                throw SimpleTrackableException(this@TestExceptionHandlers, "Suspending")
            }
        }
        assertIs<SimpleTrackableException>(registerHandlerLambda).output()
        assertIs<SimpleTrackableException>(handledBySuspendableLambda).output()
        assertEquals("Non suspending", registerHandlerLambda.message)
        assertEquals("Suspending", handledBySuspendableLambda.message)
    }

    @Test
    fun `Different exceptions caught as expected`() = runTest {


        var genericExceptionLambda: Any? = null
        registerHandler<Exception>() { exception ->
            genericExceptionLambda = exception
            exception.output()
            throw exception
        }


        var handledBySuspendableLambda: Any? = null
        registerHandler<SimpleTrackableException>() { exception ->
            handledBySuspendableLambda = exception
            exception.output()
            throw exception
        }

        var handledByScopedLambda: Any? = null
        registerHandler<ScopedException>() { exception ->
            handledByScopedLambda = exception
            exception.output()
            throw exception
        }

        assertDoesNotThrow {
            delegateIfThrow<String>(Suspended) {
                throw Exception("Exception")
            }
        }
        assertDoesNotThrow {
            delegateIfThrow<SimpleTrackableException>(Suspended) {
                throw SimpleTrackableException(this@TestExceptionHandlers, "Suspending")
            }
        }
        assertDoesNotThrow {
            delegateIfThrow(Suspended) {
                throw ScopedException(this@TestExceptionHandlers, "ScopedExceptionSuspending")
            }
        }
    }
}

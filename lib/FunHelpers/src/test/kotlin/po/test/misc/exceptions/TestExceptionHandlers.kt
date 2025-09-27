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
import po.misc.data.logging.ContextAwareLogEmitter
import po.misc.data.logging.logEmitter
import po.misc.exceptions.TrackableException
import po.misc.exceptions.TrackableScopedException
import po.misc.exceptions.delegateIfThrow
import po.misc.exceptions.metaFrameTrace
import po.misc.exceptions.models.CTXResolutionFlag
import po.misc.exceptions.models.ExceptionTrace
import po.misc.exceptions.registerHandler
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TestExceptionHandlers() : ContextAware {


    override val emitter: ContextAwareLogEmitter = logEmitter()
    override val identity: CTXIdentity<out CTX> = asIdentity()

    class SimpleTrackableException(val context: Any, message: String): Throwable(message), TrackableException{
        override val contextClass: KClass<*> get() = context::class
        override val exceptionTrace: ExceptionTrace = metaFrameTrace(contextClass)
        override val self: SimpleTrackableException = this
    }

    class ScopedException(val context: Any, message: String): Throwable(message), TrackableScopedException{
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
            channel.delegateIfThrow("suspendingThrowingBlock") {
                throw exception
            }
        }
    }

    @Test
    fun `Instantiation Functions`(){
        var registerHandlerLambda: Any? = null
        registerHandler<SimpleTrackableException> { exception->
            registerHandlerLambda = exception
        }
        var handledBySuspendableLambda: Any? = null
        registerHandler<SimpleTrackableException, String>("Some string"){exception->
            handledBySuspendableLambda = exception
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
            registerHandlerLambda = exception
        }
        var handledBySuspendableLambda: Any? = null
        registerHandler<SimpleTrackableException, String>("Some string"){exception->
            handledBySuspendableLambda = exception
        }

        assertDoesNotThrow {
            val thrower = ObjectDelegating(this@TestExceptionHandlers)
            thrower.throwingBlock(SimpleTrackableException(this@TestExceptionHandlers, "Non suspending"))
        }

        assertDoesNotThrow {
            delegateIfThrow<SimpleTrackableException>("delegateIfThrow") {
                throw SimpleTrackableException(this@TestExceptionHandlers, "Suspending")
            }
        }
        assertIs<SimpleTrackableException>(registerHandlerLambda).output()
        assertIs<SimpleTrackableException>(handledBySuspendableLambda).output()
        assertEquals("Non suspending", registerHandlerLambda.message)
        assertEquals("Suspending", handledBySuspendableLambda.message)
    }

    @Test
    fun `Different exceptions caught as expected`() = runTest{


        var  genericExceptionLambda: Any? = null
        registerHandler<Exception, String>("Exception"){ exception->
            genericExceptionLambda = exception
        }


        var handledBySuspendableLambda: Any? = null
        registerHandler<SimpleTrackableException, String>("SimpleTrackableException"){exception->
            handledBySuspendableLambda = exception
        }

        var handledByScopedLambda: Any? = null
        registerHandler<ScopedException, String>("SimpleTrackableException"){exception->
            handledByScopedLambda = exception
        }

        assertDoesNotThrow {
            delegateIfThrow<String>("delegateIfThrow") {
                throw Exception("Exception")
            }
        }
        assertDoesNotThrow {
            delegateIfThrow<SimpleTrackableException>("delegateIfThrow") {
                throw SimpleTrackableException(this@TestExceptionHandlers, "Suspending")
            }
        }
        assertDoesNotThrow {
            delegateIfThrow("Different exceptions caught as expected"){
                throw ScopedException(this@TestExceptionHandlers, "ScopedExceptionSuspending")
            }
        }

        assertIs<Exception>(genericExceptionLambda)
        val simpleTrackable = assertNotNull(handledBySuspendableLambda)
        assertIs<SimpleTrackableException>(simpleTrackable)
        val scoped =assertNotNull(handledByScopedLambda)
        assertIs<ScopedException>(scoped)
        scoped.output()
        assertEquals("Suspending", simpleTrackable.message)
        assertEquals("ScopedExceptionSuspending", scoped.message)
    }





}
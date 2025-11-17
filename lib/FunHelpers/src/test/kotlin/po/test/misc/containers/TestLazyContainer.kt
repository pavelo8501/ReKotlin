package po.test.misc.containers

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import po.misc.containers.lazy.LazyContainer
import po.misc.containers.lazy.lazyContainer
import po.misc.containers.lazy.lazyContainerOf
import po.misc.context.tracable.TraceableContext
import po.misc.data.helpers.output
import po.misc.functions.Nullable
import po.misc.io.captureOutput
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestLazyContainer: TraceableContext {

    private val initialValue = "Initial"
    private val anotherValue = "Another"
    private class ValueListener: TraceableContext

    @Test
    fun `LazyContainer does not recompute value after it is provided`() {
        val container = LazyContainer<String>()
        container.provideValue(initialValue)
        assertEquals(initialValue, container.value)
        container.provideValue("Another")
        assertEquals(initialValue, container.value, "LazyContainer should not allow overwriting value")
    }

    @Test
    fun `LazyContainer does not recompute value provided by lambda`() {
        var readCount = 1
        val container = lazyContainer<String> {
            provideValue {
                if(readCount == 1){
                    readCount ++
                    initialValue
                }else{
                    "Another"
                }
            }
        }
        assertFalse { container.valueAvailable }
        val firstRead =  assertDoesNotThrow {
            container.getValue(this)
        }
        val secondRead =  assertDoesNotThrow {
            container.getValue(this)
        }
        assertEquals(initialValue, firstRead)
        assertEquals(initialValue, secondRead)
    }

    @Test
    fun `LazyContainer valueProvided signal work as expected`() {
        val container = lazyContainerOf<String>()
        var triggerCount = 0
        for(i in 1..5){
            container.valueProvided(ValueListener()) {
                triggerCount++
            }
        }
        container.provideValue(initialValue)
        assertEquals(5, triggerCount)
    }

    @Test
    fun `Exception case provides meaningful information`() {
        val container = lazyContainerOf<String>()
        var exceptionOutput = ""
        assertThrows<IllegalStateException> {
            val captured = captureOutput(Nullable) {
                container.getValue(this)
           }
           exceptionOutput = captured.output
           captured.exception?.let {
               throw it
           }
        }
        assertTrue{
            exceptionOutput.contains("String") &&
                exceptionOutput.contains("TestLazyContainer") &&
                    exceptionOutput.contains("Exception case provides meaningful information")
        }
        exceptionOutput.output()
    }

    @Test
    fun `Value can be reassigned after reset`() {
        val container = lazyContainerOf<String>()
        container.provideValue(initialValue)
        assertNotNull(container.value)
        container.reset()
        container.provideValue(anotherValue)
        assertEquals(anotherValue, container.value)
    }

    @Test
    fun `Fallback work as expected`(){

        val providedValue = "value"
        val fallbackValue = "fallback"

        val container = LazyContainer<String>()
        container.setFallback {
            fallbackValue
        }
        assertEquals(fallbackValue, container.value)
        
        container.provideValue(providedValue)
        assertEquals(providedValue, container.value)


    }
}
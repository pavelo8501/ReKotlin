package po.test.misc.containers

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import po.misc.containers.backing.BackingContainer
import po.misc.containers.backing.backingContainer
import po.misc.containers.backing.backingContainerOf
import po.misc.context.tracable.TraceableContext
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestBackingContainer: TraceableContext {

    private class ValueListener: TraceableContext

    private val initialValue = "Initial"
    private val anotherValue = "Another"

    @Test
    fun `BackingContainer properly updates value`() {
        val container = BackingContainer<String>()
        assertNull(container.value)
        container.provideValue(initialValue)
        assertEquals(initialValue, container.value)
        container.provideValue("Another")
        assertEquals("Another", container.value)
    }


    @Test
    fun `BackingContainer recomputes value provided by lambda`() {
        var readCount = 0
        val container = backingContainer<String> {
            provideValue {
                readCount ++
                if(readCount == 1){
                    initialValue
                }else{
                    "Another"
                }
            }
        }
        val firstRead =  assertDoesNotThrow {
            container.getValue(this)
        }
        val secondRead =  container.getValue(this)
        assertEquals(2, readCount)
        assertEquals(initialValue, firstRead)
        assertEquals("Another", secondRead)
    }

    @Test
    fun `BackingContainer handles value changes as expected`() {

        val container = backingContainerOf<String>()
        val listener1 = ValueListener()
        val listener2 = ValueListener()

        val listener1Received = mutableListOf<String>()
        val listener2Received = mutableListOf<String>()

        container.getValue(listener1){
            listener1Received += it
        }
        container.getValue(listener2){
            listener2Received += it
        }
        container.provideValue(initialValue)
        var message1 = assertNotNull(listener1Received.first())
        assertEquals(initialValue, message1)
        message1 = assertNotNull(listener2Received.first())
        assertEquals(initialValue, message1)

        container.provideValue(anotherValue)
        assertTrue {
            listener1Received.size == listener2Received.size &&
                    listener2Received.size == 2
        }
        container.forget(listener1)
        container.forget(listener2)
        container.provideValue("More")

        assertTrue {
            listener1Received.size == listener2Received.size &&
                    listener2Received.size == 2
        }
    }

}
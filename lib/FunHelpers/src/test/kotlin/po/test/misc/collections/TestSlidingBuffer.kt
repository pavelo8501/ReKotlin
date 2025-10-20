package po.test.misc.collections

import org.junit.jupiter.api.Test
import po.misc.collections.BufferAction
import po.misc.collections.BufferItemStatus
import po.misc.collections.SlidingBuffer
import po.misc.collections.addToBuffer
import po.misc.data.helpers.output
import po.misc.io.captureOutput
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class TestSlidingBuffer {

    @Test
    fun `Buffer basic functionality`() {
        val buffer = SlidingBuffer<String, Unit>(this)
        buffer.add("Some string")
        assertEquals(1, buffer.size)
        assertEquals("Some string", buffer.value)
        buffer.clear()

        for (i in 1..10) {
            buffer.add("data_$i")
        }
        assertEquals(buffer.capacity, buffer.size)
        assertEquals("data_10", buffer.value)
    }

    @Test
    fun `Buffer data commit functionality`() {
        val buffer = SlidingBuffer<String, Unit>(this)
        var commited: String? = null

        buffer.onCommit {
            commited = it
        }
        buffer.onValueReceived {
            if (it.value == "commit") {
                BufferAction.Commit
            } else {
                BufferAction.Buffer
            }
        }
        buffer.add("value_1")
        buffer.add("value_2")
        buffer.add("commit")
        buffer.add("value_3")
        assertEquals(4, buffer.size)
        assertEquals("commit", commited)
    }

    @Test
    fun `Buffer same commit functionality`() {
        val buffer = SlidingBuffer<String, Unit>(this)
        var commited: String? = null

        buffer.onCommit {
            commited = it
        }
        buffer.onSameAsRecent {
            if (it.value.contains("commit")) {
                if (commited == null) {
                    BufferAction.Commit
                } else {
                    BufferAction.Buffer
                }
            } else {
                BufferAction.Buffer
            }
        }
        addToBuffer(buffer, "commit")
        addToBuffer(buffer, "commit")
        addToBuffer(buffer, "commit")
        assertEquals(3, buffer.size)
        val secondItem = assertNotNull(buffer[1])
        assertEquals(BufferItemStatus.Commited, secondItem.itemStatus)
        assertEquals("commit", secondItem.value)
        addToBuffer(buffer, "value_2")
        addToBuffer(buffer, "value_2")
        assertEquals(5, buffer.size)
        buffer.listAsInBuffer().take(2).forEach {
            assertEquals(BufferItemStatus.Buffered, it.itemStatus)
        }
    }

    @Test
    fun `Buffer helpers`() {
        val buffer = SlidingBuffer<String, Unit>(this)
        addToBuffer(buffer, "value_1")
        addToBuffer(buffer, "value_2")
        assertEquals(2, buffer.size)
    }

    @Test
    fun `Buffer warning cover all edge cases`() {
        val buffer = SlidingBuffer<String, Unit>(this)
        val output = captureOutput {
            buffer.onValueReceived {
                BufferAction.Commit
            }
            buffer.add("SomeValue")
        }
        output.output()
        buffer.toString().output()
        assertTrue(output.output.contains("Received Commit command but no onCommit function"), "Real output: $output")
    }

    @Test
    fun `Last buffered value can be commited on command`() {
        var commited: String? = null
        val buffer = SlidingBuffer<String, Unit>(this)
        buffer.onCommit {
            commited = it
        }
        buffer.onValueReceived {
            if(it.value == "value_1"){
                BufferAction.Commit
            }else{
                BufferAction.Buffer
            }
        }
        addToBuffer(buffer, "value_1")
        addToBuffer(buffer, "buffered_to_flush")
        val firstItem = assertNotNull(buffer.listOldestFirst().firstOrNull())
        val lastItem = assertNotNull(buffer.listOldestFirst().lastOrNull())
        assertEquals(BufferItemStatus.Commited, firstItem.itemStatus)
        assertEquals(BufferItemStatus.Buffered, lastItem.itemStatus)
        buffer.flush()
        assertEquals(BufferItemStatus.Commited, lastItem.itemStatus)
    }

}
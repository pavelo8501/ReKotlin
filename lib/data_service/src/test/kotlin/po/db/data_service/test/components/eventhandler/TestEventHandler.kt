package po.db.data_service.test.components.eventhandler

import org.junit.jupiter.api.Test
import po.db.data_service.components.eventhandler.EventHandler
import po.db.data_service.components.eventhandler.RootEventHandler
import po.db.data_service.components.eventhandler.enums.EventType
import kotlin.test.assertContains
import kotlin.test.assertEquals

class TestEventHandler {

    @Test
    fun `event registration logic`(){
        val rootHandlerMock = RootEventHandler("Root Module")

        rootHandlerMock.notify("Notification")
        rootHandlerMock.notifyError("Error")

        assertEquals(rootHandlerMock.eventQue.count(), 2)
        assertEquals(rootHandlerMock.eventQue[0].type, EventType.INFO)
        assertEquals(rootHandlerMock.eventQue[1].type, EventType.ERROR)
    }

    @Test
    fun `nested event handling logic with no parent event created`(){
        val rootHandlerMock = RootEventHandler("Root Module")
        val subRootHandlerMock = EventHandler("Some Module", rootHandlerMock)
        val subSubHandlerMock = EventHandler("Some Module 2", subRootHandlerMock)

        subSubHandlerMock.notify("something happened")

        assertEquals(rootHandlerMock.eventQue.count(), 1)
        assertEquals(rootHandlerMock.eventQue[0].type, EventType.INFO)
        assertContains(rootHandlerMock.eventQue[0].module, "Some Module 2")
    }

    @Test
    fun `nested event handling logic with parent event created`(){
        val rootHandlerMock = RootEventHandler("Root Module")
        val subRootHandlerMock = EventHandler("Some Module", rootHandlerMock)
        val subSubHandlerMock = EventHandler("Some Module 2", subRootHandlerMock)

        rootHandlerMock.notify("something happened on parent")
        subSubHandlerMock.notify("something happened on last in chain")

        assertEquals(rootHandlerMock.eventQue.count(), 1)
        assertEquals(rootHandlerMock.eventQue[0].type, EventType.INFO)
        assertEquals(rootHandlerMock.eventQue[0].msg, "something happened on parent")
        assertEquals(rootHandlerMock.eventQue[0].subEvents.count(), 1)
        assertEquals(rootHandlerMock.eventQue[0].subEvents[0].msg, "something happened on last in chain")
        assertEquals(rootHandlerMock.eventQue[0].subEvents[0].type, EventType.INFO)
    }

}
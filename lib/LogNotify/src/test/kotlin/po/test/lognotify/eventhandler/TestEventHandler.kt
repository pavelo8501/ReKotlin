package po.test.lognotify.eventhandler

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import po.lognotify.eventhandler.exceptions.CancelException
import po.lognotify.eventhandler.exceptions.PropagateException
import po.lognotify.eventhandler.exceptions.SkipException
import po.lognotify.shared.enums.SeverityLevel
import po.test.lognotify.testmodels.HostingObject
import po.test.lognotify.testmodels.ParentHostingObject
import po.test.lognotify.testmodels.SubHostingObject
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestEventHandler {

    companion object {

        private  lateinit var  parentObject : ParentHostingObject

        @BeforeAll
        @JvmStatic
        fun setup() {
            parentObject = ParentHostingObject("ParentHostingObject")
            parentObject.also{
                it.childObjects.add(HostingObject("Child Module 1", parentObject))
                it.childObjects.add(HostingObject("Child Module 2", parentObject))
            }
        }
    }

    @Test
    fun `event registration logic`(){
        runBlocking {
            launch {
                parentObject.mockTaskRun(1){
                    infoMessage("Register Info Message")
                    notifyError("Error")
                }
                assertEquals(parentObject.eventHandler.eventQue.count(), 3)
                assertEquals(parentObject.eventHandler.eventQue[0].type, SeverityLevel.INFO)
                assertEquals(parentObject.eventHandler.eventQue[1].type, SeverityLevel.INFO)
                assertEquals(parentObject.eventHandler.eventQue[2].type, SeverityLevel.EXCEPTION)
            }
        }
    }

    @Test
    fun `nested event handling logic with no parent event created`(){
        val childObject1 =  parentObject.childObjects[0]
        val childObject2 =  parentObject.childObjects[1]
        runBlocking {
           launch {
                childObject1.infoMessage("Something happened")
                childObject2.infoMessage("Something happened 2")

                assertEquals(1, parentObject.eventHandler.eventQue.count())
                assertEquals(parentObject.eventHandler.eventQue[0].type, SeverityLevel.INFO)
                assertContains(parentObject.eventHandler.eventQue[0].module, "ParentHostingObject")
                assertEquals(2, parentObject.eventHandler.eventQue[0].subEvents.count())
                assertContains(parentObject.eventHandler.eventQue[0].module,"ParentHostingObject")
                assertContains(parentObject.eventHandler.eventQue[0].subEvents[0].module,"Child Module 1")
                assertEquals("Something happened", parentObject.eventHandler.eventQue[0].subEvents[0].msg)
                assertContains(parentObject.eventHandler.eventQue[0].subEvents[1].module, "Child Module 2")
                assertEquals("Something happened 2", parentObject.eventHandler.eventQue[0].subEvents[1].msg)
            }
        }
    }

    @Test
    fun `nested event handling logic with parent event created`() {
        runBlocking {
            launch {
                parentObject.eventHandler.info("something happened on parent")
                parentObject.childObjects[0].apply {
                    SubHostingObject("Sub Child Module 1", this).let {
                        subObjects.add(it)
                        it.infoMessage("something happened on last in chain")
                    }
                }
                assertEquals(2, parentObject.eventHandler.eventQue.count())
                assertEquals(SeverityLevel.INFO, parentObject.eventHandler.eventQue[1].type)
                assertContains(
                    parentObject.eventHandler.eventQue[1].subEvents[0].msg,
                        "last in chain")
                assertEquals(SeverityLevel.INFO, parentObject.eventHandler.eventQue[1].subEvents[0].type)
            }
        }
    }

    @Test
    fun `measurements taken in action event`(){
        var result = ""
        runBlocking {
            launch {
                parentObject.action("TestPass") {
                    result =  parentObject.passData("Test")
                }
            }
        }
        assertEquals("Test", result)
        assertEquals(SeverityLevel.EVENT, parentObject.eventHandler.eventQue[1].type)
        assertEquals("TestPass", parentObject.eventHandler.eventQue[1].msg)
        assertTrue(parentObject.eventHandler.eventQue[1].elapsed > 0)
    }

    @Test
    fun `check can throw appropriate exceptions`(){

        parentObject.eventHandler.apply {
            registerSkipException {
                SkipException("DefaultMessage")
            }
            registerCancelException {
                CancelException("DefaultMessage")
            }
            registerPropagateException {
                PropagateException("DefaultMessage")
            }
        }

        assertThrows<SkipException>(){
            parentObject.eventHandler.raiseSkipException("Skip Message")
        }
        assertThrows<CancelException>() {
            parentObject.eventHandler.raiseCancelException("Cancel Message")
        }
        assertThrows<PropagateException> {
            parentObject.eventHandler.raisePropagateException("Propagate Message")
        }

    }

}
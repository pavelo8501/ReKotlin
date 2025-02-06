package po.lognotify.test.eventhandler

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import po.lognotify.eventhandler.exceptions.CancelException
import po.lognotify.eventhandler.exceptions.PropagateException
import po.lognotify.eventhandler.exceptions.SkipException
import po.lognotify.shared.enums.HandleType
import po.lognotify.shared.enums.SeverityLevel
import po.lognotify.test.testmodels.HostingObject
import po.lognotify.test.testmodels.ParentHostingObject
import po.lognotify.test.testmodels.SubHostingObject
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
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
                    result = "Test"
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
                SkipException("Skip Default Message")
            }
            registerCancelException {
                CancelException("Cancel Default Message")
            }
            registerPropagateException {
                PropagateException("Propagate Default Message")
            }
        }

        val skipException =  assertThrows<SkipException>{
            parentObject.eventHandler.raiseSkipException("Skip Message")
        }
        assertEquals("Skip Message", skipException.message)
        assertEquals(HandleType.SKIP_SELF, skipException.handleType)

        val cancelException = assertThrows<CancelException>() {
            parentObject.eventHandler.raiseCancelException("Cancel Message"){

            }
        }
        assertEquals("Cancel Message", cancelException.message)
        assertEquals(HandleType.CANCEL_ALL, cancelException.handleType)

        val propagateException = assertThrows<PropagateException> {
            parentObject.eventHandler.raisePropagateException<PropagateException>("Propagate Message"){
                throw this
            }
        }
        assertEquals("Propagate Message", propagateException.message)
        assertEquals(HandleType.PROPAGATE_TO_PARENT, propagateException.handleType)
    }

    @Test
    fun `test skip exception logic`(){
        parentObject.eventHandler.apply {
            registerSkipException {
                SkipException("Skip Default Message")
            }
        }
        val child1 = parentObject.childObjects[0]
        child1.eventHandler.registerSkipException {
            SkipException("Skip Message1")
        }

        runBlocking {
            launch {
                var effectiveI = 0
                var effectiveA = 0
                var processedA = mutableListOf<Int>()
                parentObject.mockTaskRun(0) {
                    for (i in 0..1) {
                        effectiveI = i
                        for (a in 0..4) {
                            child1.mockTaskRun(0, i) {
                                child1.eventHandler.action("TestAction") {
                                    if (a == 1) {
                                        eventHandler.raiseSkipException("Skip Message")
                                    }
                                    effectiveA = a
                                    processedA.add(a)
                                }
                            }
                        }
                    }
                }
                assertEquals(4, effectiveA)
                assertFalse(processedA.contains(1))
                parentObject.eventHandler.currentEvent!!.subEvents[1].let {
                    assertNotNull(it)
                    assertEquals(SeverityLevel.EXCEPTION, it.type)
                    assertEquals("Skip Message", it.msg)
                }
            }
        }
    }

    @Test
    fun `test propagate exception logic`(){
        parentObject.eventHandler.apply {
            registerSkipException {
                SkipException("Skip Default Message")
            }
        }
        val child1 = parentObject.childObjects[0]
        child1.eventHandler.registerCancelException {
            CancelException("Cancel Message1")
        }

        runBlocking {
            launch {
                var effectiveI = 0
                var effectiveA = 0
                var processedA = mutableListOf<Int>()

                parentObject.mockTaskRun(0) {
                    for (i in 0..1) {
                        effectiveI = i
                        var continueLoop = true
                        for (a in 0..4) {
                            if(continueLoop == false){
                                break
                            }
                            child1.mockTaskRun(0, i) {
                                child1.eventHandler.action("TestAction") {
                                    if (a == 2) {
                                        eventHandler.raiseCancelException("Cancel Message"){
                                            continueLoop = false
                                        }
                                    }
                                    effectiveA = a
                                    processedA.add(a)
                                }
                            }
                        }
                    }
                }
                assertEquals(1, effectiveA)
                assertEquals(4,processedA.count())
                parentObject.eventHandler.currentEvent!!.subEvents[2].let {
                    assertNotNull(it)
                    assertEquals(SeverityLevel.EXCEPTION, it.type)
                    assertEquals("Cancel Message", it.msg)
                }
            }
        }
    }
}
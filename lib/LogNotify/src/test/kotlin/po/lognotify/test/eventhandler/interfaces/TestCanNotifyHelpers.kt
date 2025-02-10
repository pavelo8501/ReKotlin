package po.lognotify.test.eventhandler.interfaces

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import po.lognotify.eventhandler.exceptions.ProcessableException
import po.lognotify.shared.enums.HandleType
import po.lognotify.test.testmodels.ParentHostingObject
import po.lognotify.test.testmodels.TestCancelException
import po.lognotify.test.testmodels.TestException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestCanNotifyHelpers {

    @Test
    fun `convenience method throwSkip throws correct exception`(){
       val ex = assertThrows<ProcessableException> {
            val rootHostingObject = ParentHostingObject("root")
            rootHostingObject.exposeSelf{
               this@exposeSelf.eventHandler.registerSkipException<TestException>() {
                   TestException("default", 1)
               }
               throwSkip("skip")
            }
        }
        assertEquals("skip", ex.message)
        assertEquals(HandleType.SKIP_SELF, ex.handleType)
    }

    @Test
    fun `convenience method throwPropagate throws correct exception`(){

        val ex = assertThrows<ProcessableException> {
            val rootHostingObject = ParentHostingObject("root")
            rootHostingObject.exposeSelf{
                this@exposeSelf.eventHandler.registerPropagateException<TestException>() {
                    TestException("default_propagate", 1)
                }
                throwPropagate("propagate")
            }
        }
        assertEquals("propagate", ex.message)
        assertEquals(HandleType.PROPAGATE_TO_PARENT, ex.handleType)
    }

    @Test
    fun `convenience method throwCancel throws correct exception`(){
        var cancelFired = false
        val cancelFn : ()-> Unit = {
            cancelFired = true
        }
        val rootHostingObject = ParentHostingObject("root")

        val ex = assertThrows<ProcessableException> {
            rootHostingObject.exposeSelf{
                this@exposeSelf.eventHandler.registerCancelException<TestCancelException>(cancelFn){
                    TestCancelException("default_cancel")
                }
                throwCancel("cancel")
            }
        }
        assertEquals("cancel", ex.message)
        assertEquals(HandleType.CANCEL_ALL, ex.handleType)
        assertTrue(cancelFired)


        var cancelOnThrow = false
        assertThrows<ProcessableException> {
            rootHostingObject.exposeSelf {
                throwCancel("cancel_manual") {
                    cancelOnThrow = true
                }
            }
        }
        assertTrue(cancelOnThrow)




    }

}
package po.test.lognotify

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import po.test.lognotify.testmodels.HostingObject
import po.test.lognotify.testmodels.ParentHostingObject

class TestEventHandler {

    companion object {

        private  lateinit var  parentObject : ParentHostingObject

        @BeforeAll
        @JvmStatic
        fun setup() {

            parentObject = ParentHostingObject()
            parentObject.also{
                it.subObjects.add(HostingObject(1,parentObject))
                it.subObjects.add(HostingObject(2,parentObject))
            }
        }
    }

    @Test
    fun `test event handler`(){

    }

}
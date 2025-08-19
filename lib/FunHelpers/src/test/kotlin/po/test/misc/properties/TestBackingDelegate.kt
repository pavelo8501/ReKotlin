package po.test.misc.properties

import org.junit.jupiter.api.Test

import po.misc.properties.delegates.BackingDelegate
import po.test.misc.setup.ControlClass
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestBackingDelegate {

    @Test
    fun `Backing delegate's hooks work as expected`(){

        var isValueSet: Boolean? = null
        val sourceClass = ControlClass()
        var fromOnChangeHook: Any? = null

        var delegate by BackingDelegate.withHooks<ControlClass> {
            onBeforeInitialized {delegate->
                isValueSet = delegate.isValueSet
                print(delegate.contextName)
            }
            onInitialized { delegate->
                isValueSet = delegate.isValueSet
            }
            onChanged { change->
                fromOnChangeHook = change.newValue
            }
        }

        val checkedIsValueSet = assertNotNull(isValueSet, "onBeforeInit never triggered")
        assertFalse(checkedIsValueSet, "Delegate's isValueSet property  wrong")
        delegate = sourceClass
        assertTrue(isValueSet!!, "Delegate's isValueSet does not update on actual value provided")

        val checkedValue = assertIs<ControlClass>(fromOnChangeHook, "Value provided by onChange mismatch")
        assertEquals(sourceClass, checkedValue, "This is not exactly same instance")
    }
}
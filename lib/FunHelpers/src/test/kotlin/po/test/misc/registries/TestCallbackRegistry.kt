package po.test.misc.registries

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.interfaces.ValueBased
import po.misc.registries.callback.TypedCallbackRegistry
import kotlin.test.assertEquals

class TestCallbackRegistry {

    enum class CallbackType(override val value: Int) :  ValueBased{
        ON_START(1),
        ON_FINISH(2);
    }
    class Component(var sourceName: String
    ) : CTX{

        override val identity: CTXIdentity<out CTX> = asIdentity()
         override val contextName: String = "ssss"
    }

   data class ValueRecord(
        val newValue : Int,
        val oldValue : Int
    )
    
//    @Test
//    fun `TypedCallbackRegistry register and invoke`(){
//
//        val component1 = Component("Component1")
//        val component2 = Component("Component2")
//
//        val registry = TypedCallbackRegistry<ValueRecord, Unit>()
//
//        var subscription1StartNewValue: Int = 0
//        var subscription1StartOldValue: Int = 0
//
//        var subscription1FinishNewValue: Int = 0
//        var subscription1FinishOldValue: Int = 0
//
//        var subscription2StartNewValue: Int = 0
//        var subscription2StartOldValue: Int = 0
//
//        var subscription2FinishNewValue: Int = 0
//        var subscription2FinishOldValue: Int = 0
//
//
//        registry.subscribe(component1, CallbackType.ON_START){rec->
//            subscription1StartNewValue = rec.newValue
//            subscription1StartOldValue = rec.oldValue
//        }
//        registry.subscribe(component1, CallbackType.ON_FINISH){rec->
//            subscription1FinishNewValue = rec.newValue
//            subscription1FinishOldValue = rec.oldValue
//        }
//
//        registry.subscribe(component2, CallbackType.ON_START){rec->
//            subscription2StartNewValue = rec.newValue
//            subscription2StartOldValue = rec.oldValue
//        }
//        registry.subscribe(component2, CallbackType.ON_FINISH){rec->
//            subscription2FinishNewValue = rec.newValue
//            subscription2FinishOldValue = rec.oldValue
//        }
//
//        registry.trigger(component1, CallbackType.ON_START, ValueRecord(2, 1))
//        registry.trigger(component1, CallbackType.ON_FINISH, ValueRecord(3, 2))
//
//        registry.trigger(component2, CallbackType.ON_START, ValueRecord(20, 10))
//        registry.trigger(component2, CallbackType.ON_FINISH, ValueRecord(30, 20))
//
//        assertAll("Callbacks triggered by component and key",
//
//            { assertEquals(2, subscription1StartNewValue, "Component1 OnStart NewValue mismatch") },
//            { assertEquals(1, subscription1StartOldValue, "Component1 OnStart OldValue mismatch") },
//            { assertEquals(3, subscription1FinishNewValue, "Component1 ON_FINISH NewValue mismatch") },
//            { assertEquals(2, subscription1FinishOldValue, "Component1 ON_FINISH OldValue mismatch") },
//
//            { assertEquals(20, subscription2StartNewValue, "Component2 OnStart NewValue mismatch") },
//            { assertEquals(10, subscription2StartOldValue, "Component2 OnStart OldValue mismatch") },
//            { assertEquals(30, subscription2FinishNewValue, "Component2 ON_FINISH NewValue mismatch") },
//            { assertEquals(20, subscription2FinishOldValue, "Component2 ON_FINISH OldValue mismatch") }
//        )
//
//        registry.triggerForAll(CallbackType.ON_START, ValueRecord(200, 100))
//        registry.triggerForAll(CallbackType.ON_FINISH, ValueRecord(300, 200))
//
//
//        assertAll("Callbacks triggered by key",
//
//            { assertEquals(200, subscription1StartNewValue, "Component1 OnStart NewValue mismatch") },
//            { assertEquals(100, subscription1StartOldValue, "Component1 OnStart OldValue mismatch") },
//            { assertEquals(300, subscription1FinishNewValue, "Component1 ON_FINISH NewValue mismatch") },
//            { assertEquals(200, subscription1FinishOldValue, "Component1 ON_FINISH OldValue mismatch") },
//
//            { assertEquals(200, subscription2StartNewValue, "Component2 OnStart NewValue mismatch") },
//            { assertEquals(100, subscription2StartOldValue, "Component2 OnStart OldValue mismatch") },
//            { assertEquals(300, subscription2FinishNewValue, "Component2 ON_FINISH NewValue mismatch") },
//            { assertEquals(200, subscription2FinishOldValue, "Component2 ON_FINISH OldValue mismatch") }
//        )
//
//    }
//
//    @Test
//    fun `CallbackRegistry register and invoke`(){
//
//        val component1 = Component("Component1")
//        val component2 = Component("Component2")
//
//        val registry = CallbackRegistry()
//
//        var subscription1STARTTriggered: Int = 0
//        var subscription1FinishTriggered: Int = 0
//        var subscription2STARTTriggered: Int = 0
//        var subscription2FinishTriggered: Int = 0
//
//        registry.subscribe(component1, CallbackType.ON_START){
//            subscription1STARTTriggered++
//        }
//        registry.subscribe(component1, CallbackType.ON_FINISH){
//            subscription1FinishTriggered++
//        }
//        registry.subscribe(component2, CallbackType.ON_START){
//            subscription2STARTTriggered++
//        }
//        registry.subscribe(component2, CallbackType.ON_FINISH){
//            subscription2FinishTriggered++
//        }
//
//        registry.trigger(component1, CallbackType.ON_START)
//        registry.trigger(component1, CallbackType.ON_FINISH)
//        registry.trigger(component2, CallbackType.ON_START)
//        registry.trigger(component2, CallbackType.ON_FINISH)
//
//        registry.trigger(component2, CallbackType.ON_START)
//
//        assertAll("Callbacks triggered by component and key",
//
//            { assertEquals(1, subscription1STARTTriggered, "Component1 OnStart failed") },
//            { assertEquals(1, subscription1FinishTriggered, "Component1 OnFinish failed") },
//            { assertEquals(2, subscription2STARTTriggered, "Component2 OnStart failed") },
//            { assertEquals(1, subscription2FinishTriggered, "Component2 OnFinish failed") }
//        )
//    }



}
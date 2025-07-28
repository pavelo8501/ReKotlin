package po.test.misc.functions

import org.junit.jupiter.api.Test
import po.misc.functions.containers.DeferredContainer
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.functions.containers.LambdaHolder
import kotlin.test.assertEquals

class TestReactiveHooks: CTX {

    override val identity: CTXIdentity<out CTX> = asIdentity()


    @Test
    fun `Reactive hooks work as expected`(){

        val inputString = "TestString"

        var onProviderSetBlockExecuted = false
        var onChangeBlockExecuted = false
        var onBeforeResolveBlockExecuted = false
        var onResolvedBlockExecuted = false
        var withResolvedValueBlockExecuted = false
        var onDisposeBlockExecuted = false

        var valueProvided = ""

        val lambdaContainer = LambdaHolder<String>(this)

        lambdaContainer.hooks.onProviderSet {
            onProviderSetBlockExecuted = true
            assertEquals(false, it.isResolved, "isResolved should be false in OnProviderSet block")
            assertEquals(true, it.isLambdaProvided, "isLambdaProvided should be true in OnProviderSet block")
        }

        lambdaContainer.hooks.onBeforeResolve {
            assertEquals(false, it.isValueAvailable, "isValueAvailable in OnBeforeResolve block should be false")
            onBeforeResolveBlockExecuted = true
        }

        lambdaContainer.hooks.onChange {old,new->
            onChangeBlockExecuted = true
            valueProvided = new
        }

        lambdaContainer.hooks.onResolved {
            onResolvedBlockExecuted = true
            valueProvided = it
        }
        lambdaContainer.hooks.withResolvedValue {
            withResolvedValueBlockExecuted = true
            assertEquals(inputString, this, "InputString and hooks this mismatch in WithResolvedValue")
        }

        lambdaContainer.hooks.onDispose {
            onDisposeBlockExecuted = true
            assertEquals(false, it.isValueAvailable, "OnDispose hook isValueAvailable should be false")
        }

        lambdaContainer.registerProvider {

        }

        assertEquals(true, onProviderSetBlockExecuted, "OnProviderSetBlockExecuted hook never triggered")

        lambdaContainer.provideReceiver(inputString)
        assertEquals(true, onChangeBlockExecuted, "OnChangeBlock hook never triggered")
        assertEquals(inputString, valueProvided, "ValueProvided and InputString mismatch")

        lambdaContainer.dispose(withHooks =  false)
        assertEquals(true, onDisposeBlockExecuted, "OnDisposeBlockExecuted hook never triggered")

        lambdaContainer.registerProvider {

        }

        lambdaContainer.resolve(inputString)
        assertEquals(true, onBeforeResolveBlockExecuted, "OnBeforeResolveBlockExecuted hook never triggered")
        assertEquals(true, onResolvedBlockExecuted, "OnResolved hook never triggered")
        assertEquals(true, withResolvedValueBlockExecuted, "WithResolvedValueBlock never executed")
        assertEquals(inputString, valueProvided, "ValueProvided and InputString mismatch")
    }


    @Test
    fun `Reactive hooks on DeferredContainer show same behaviour as with LambdaContainer `(){

        val inputString = "ResultString"

        var onProviderSetBlockExecuted = false
        var onChangeBlockExecuted = false
        var onBeforeResolveBlockExecuted = false
        var onResolvedBlockExecuted = false
        var withResolvedValueBlockExecuted = false
        var onDisposeBlockExecuted = false

        var valueProvided = ""

        val deferred = DeferredContainer<String>(this)

        deferred.hooks.onProviderSet {
            onProviderSetBlockExecuted = true
            assertEquals(false, it.isResolved, "isResolved should be false in OnProviderSet block")
            assertEquals(true, it.isLambdaProvided, "isLambdaProvided should be true in OnProviderSet block")
        }

        deferred.hooks.onBeforeResolve {
            assertEquals(false, it.isValueAvailable, "isValueAvailable in OnBeforeResolve block should be false")
            onBeforeResolveBlockExecuted = true
        }

        deferred.hooks.onChange {old,new->
            onChangeBlockExecuted = true
            valueProvided = new
        }

        deferred.hooks.onResolved {
            onResolvedBlockExecuted = true
            valueProvided = it
        }
        deferred.hooks.withResolvedValue {
            withResolvedValueBlockExecuted = true
            assertEquals(inputString, this, "InputString and hooks this mismatch in WithResolvedValue")
        }

        deferred.hooks.onDispose {
            onDisposeBlockExecuted = true
            assertEquals(false, it.isValueAvailable, "OnDispose hook isValueAvailable should be false")
        }

        deferred.registerProvider {
            inputString
        }

        deferred.resolve()

        assertEquals(true, onProviderSetBlockExecuted, "OnProviderSetBlockExecuted hook never triggered")

        assertEquals(true, onChangeBlockExecuted, "OnChange hook never triggered")

        assertEquals(inputString, valueProvided, "ValueProvided and InputString mismatch")

        deferred.dispose(false)
        assertEquals(true, onDisposeBlockExecuted, "OnDispose hook never triggered")

        deferred.registerProvider {
            inputString
        }

        assertEquals(true, onBeforeResolveBlockExecuted, "OnBeforeResolveBlockExecuted hook never triggered")
        assertEquals(true, onResolvedBlockExecuted, "OnResolvedBlockExecuted hook never triggered")
        assertEquals(true, withResolvedValueBlockExecuted, "WithResolvedValueBlock never executed")
        assertEquals(inputString, valueProvided, "ValueProvided and InputString mismatch")
    }

}
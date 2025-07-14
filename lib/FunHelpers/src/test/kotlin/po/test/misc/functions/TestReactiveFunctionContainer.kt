package po.test.misc.functions

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import po.misc.functions.containers.DeferredContainer
import po.misc.functions.containers.LambdaContainer
import po.misc.interfaces.CTX
import po.test.misc.setup.ControlClass
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestReactiveFunctionContainer {

    class HoldingClass(): ControlClass(), CTX{

        override val contextName: String
            get() = "HoldingClass[TestFunContainers]"

        private var result: Int? = null

        val withInputFNContainer: LambdaContainer<String> = LambdaContainer(this)
        val resultFNContainer: DeferredContainer<Int> = DeferredContainer(this)

        init {
            resultFNContainer.registerProvider{
                val inputValue = withInputFNContainer.value
                withInputFNContainer.resolve(inputValue)
                inputValue.count()
            }
        }

        fun provideInput(value: String){
            withInputFNContainer.provideReceiver(value)
        }
    }

    fun HoldingClass.withInput(block: String.()-> Unit){
        withInputFNContainer.registerProvider(block)
    }

    fun HoldingClass.getResult(): DeferredContainer<Int> = resultFNContainer


    @Test
    fun `DeferredContainer execution logic work as expected`(){

        val inputString = "TestInput"

        val holdingClass =  HoldingClass()
        var triggeredWithInput: String = ""
        var triggeredResult: Int = 0

        holdingClass.withInput {
            triggeredWithInput = this
        }

        assertTrue(triggeredWithInput.isEmpty(), "WithInputLambda triggered before explicit call")
        assertEquals(false, holdingClass.withInputFNContainer.isResolved, "Wrong isResolved indicator")
        assertEquals(0,  triggeredResult, "ResultLambda triggered before explicit call")
        assertEquals(false, holdingClass.resultFNContainer.isResolved, "Wrong ResultLambda isResolved indicator")

        holdingClass.provideInput(inputString)
        assertAll("Input provided but no lambdas should be triggered",
            { assertTrue(triggeredWithInput.isEmpty(), "WithInputLambda triggered before explicit call") },
            { assertEquals(true, holdingClass.withInputFNContainer.isResolved, "isResolved should be true since lambda and receiver provided") },
            { assertEquals(0,  triggeredResult, "ResultLambda triggered before explicit call") },
            { assertEquals(
                false,
                holdingClass.resultFNContainer.isResolved,
                "isResolved of resultFNContainer<DeferredContainer<Int>> should be false since no execution call being made") }
        )

        triggeredResult = holdingClass.resultFNContainer.resolve()

        assertAll("Input provided but no lambdas should be triggered",
          { assertEquals(inputString.count(), triggeredResult, "Wrong result returned by DeferredContainer<Int>") }
        )
    }

}
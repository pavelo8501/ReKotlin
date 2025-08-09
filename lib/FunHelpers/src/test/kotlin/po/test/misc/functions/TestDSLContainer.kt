package po.test.misc.functions

import org.junit.jupiter.api.Test
import po.misc.containers.Containable
import po.misc.containers.ReceiverContainer
import po.misc.functions.dsl.DSLBuildingBlock
import po.misc.functions.dsl.runOnReceiver
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull


class TestDSLContainer{

    internal class Receiver: DSLBuildingBlock{
        var string: String = ""
        val receivers2: MutableList<Receiver2> = mutableListOf()
    }
    internal class Receiver2:DSLBuildingBlock{
        var otherString: String = ""
    }

    internal class DSLBlock<T: Any> (
        override val receiver: T
    ): ReceiverContainer<T>{
        fun createWithBuilder(builder:T.()-> Unit):T{
           receiver.builder()
           return receiver
        }
    }
    internal fun <T: DSLBuildingBlock> T.createReceiver2(block: Receiver2.()-> Unit): T{
        Receiver2().apply(block)
        return  this
    }

    @Test
    fun `DSL container swaps context`(){

        val inputValue = "Some Value"
        val inputValue2 = "Some Value2"
        val receiver1 = Receiver()
        val dslBlock : DSLBlock<Receiver> = DSLBlock(receiver1)

        val result =  dslBlock.createWithBuilder {
            runOnReceiver {
                string = inputValue
            }
            createReceiver2 {
                otherString = inputValue2
                receivers2.add(this)
            }
        }

        assertIs<Receiver>(result)
        assertEquals(inputValue, result.string)
        val receiver2 = assertNotNull(result.receivers2[0])
        assertEquals(inputValue2, receiver2.otherString)

    }

}
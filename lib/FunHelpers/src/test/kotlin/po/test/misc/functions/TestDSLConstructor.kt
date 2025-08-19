package po.test.misc.functions

import org.junit.jupiter.api.Test
import po.misc.functions.dsl.ConstructableDSL
import po.misc.functions.dsl.helpers.dslConstructor
import po.misc.functions.dsl.helpers.nextBlock
import po.test.misc.setup.ControlClass
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestDSLConstructor() : ConstructableDSL<TestDSLConstructor, String> {

    internal val dslConstructor = dslConstructor()

    class DerivedControlClass(val extra: String ="ExtraClass"): ControlClass()

    val testName: String = "TestDSLContainer3"
    val controlClass = ControlClass(property1 = "propertyOnControlClass")
    val extraClass  : DerivedControlClass = DerivedControlClass()


    @Test
    fun `DSL basic functionality work as expected`() {

        dslConstructor.build {
            addBlock{
                testName
            }
            addBlock{
                controlClass.property3
            }
            addSubBlock({ controlClass }) {
                property1
            }
        }

        assertEquals(2, dslConstructor.dslDSLBlocks.size)

        val result = dslConstructor.resolve(this)
        println(result)
        assertEquals(3, result.size, "Resolution failed")
    }

    @Test
    fun `DSL attached helpers work as expected`() {

        dslConstructor.build{
            nextBlock {
                testName
            }
            nextBlock({ extraClass }){
                extra
            }
            nextBlock({ controlClass }) {
                property1
            }
        }

        assertEquals(1, dslConstructor.dslDSLBlocks.size)
        assertEquals(2, dslConstructor.subBlocks.size)
        assertEquals(3, dslConstructor.blocksTotalCount)

        val result = dslConstructor.resolve(this)
        assertEquals(3, result.size, "Resolution failed")
    }

    @Test
    fun `DSL with Handler work as expected`() {
        dslConstructor.dslHandler.applyToResult {
            "[$it]"
        }
        dslConstructor.build{
            nextBlock {
                testName
            }
            nextBlock({ extraClass }){
                extra
            }
            nextBlock({ controlClass }) {
                property1
            }
        }

        val result = dslConstructor.resolve(this)
        println(result)
        assertEquals(3, result.size)
        assertTrue((result[0].contains("[") && result[0].contains("]")))
    }

}
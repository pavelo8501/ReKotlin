package po.test.misc.functions

import org.junit.jupiter.api.Test
import po.misc.functions.containers.NullableProvider
import po.misc.functions.dsl.DSLBuilder
import po.misc.functions.dsl.DSLContainer
import po.misc.functions.dsl.dslBuilder
import po.test.misc.functions.TestDSLContainer.TestDSLControlClass
import po.test.misc.setup.ControlClass
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestDSLContainer() : DSLBuilder<TestDSLControlClass, String> {


    override val dslContainer: DSLContainer<TestDSLControlClass, String> = DSLContainer()

    class TestDSLSubClass(val property : String)

    class TestDSLControlClass(val subClassProperty : TestDSLSubClass):ControlClass()

    class Starter():DSLBuilder<TestDSLControlClass, String>{
        override val dslContainer: DSLContainer<TestDSLControlClass, String> = DSLContainer()

    }



    @Test
    fun `DSLContainer preEvaluate self constructing lambda, structure being created`() {

        val dslContainer = DSLContainer<TestDSLControlClass, String> {

            next { property1 }
            next { property3 }
            with({ it.subClassProperty }) {
                next { property }
            }
        }
        dslContainer.build()

        assertEquals(1, dslContainer.subContainersCount, "\"with\" method does not persist container")
        assertEquals(2, dslContainer.dslBlocks.size, "\"next\" method does not persist dslBlocks")
        assertEquals(3, dslContainer.dslBlocksTotalSize, "Total dslBlockCount mismatch")
    }

    @Test
    fun `DSLContainer resolves pre saved lambda with actual data correctly`(){

        val sourceClass = TestDSLControlClass(subClassProperty = TestDSLSubClass("subClassProperty"))
        val dslContainer = DSLContainer<TestDSLControlClass, String>{
            next {
                property1
            }
            with({ it.subClassProperty }){
                next {
                    property
                }
            }
        }
        dslContainer.build()

        val resultAsList = dslContainer.resolve(sourceClass)
        assertEquals(2, resultAsList.size, "resolve method failed")

        val testClass = TestDSLControlClass(subClassProperty = TestDSLSubClass("SubTest"))
        testClass.property1 = "Test"

        val convertedResult = dslContainer.resolve(testClass){list->
            list.joinToString(separator = "/") { it }
        }
        assertEquals("Test/SubTest", convertedResult, "Resulting string mismatch")
    }

    @Test
    fun `DSLContainer convenience function work as expected`(){

        val testClass = TestDSLControlClass(subClassProperty = TestDSLSubClass("SubTest"))
        testClass.property1 = "Test"
         dslBuilder {
            next {
                property1
            }
            with({ it.subClassProperty }){
                next {
                    property
                }
            }
        }
        val convertedResult = dslContainer.resolve(testClass){list->
            list.joinToString(separator = "/") { it }
        }
        assertEquals("Test/SubTest", convertedResult, "Resulting string mismatch")
    }

}
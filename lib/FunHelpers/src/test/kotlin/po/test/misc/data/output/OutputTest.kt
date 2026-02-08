package po.test.misc.data.output

import po.misc.data.output.*
import po.misc.data.strings.contains
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.TextStyler
import po.misc.data.styles.colorize
import po.misc.io.captureOutput
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OutputTest: TextStyler {

    private class Instance

    @Test
    fun `Output block`(){
        val captured = captureOutput {
            outputBlock("OutputTest") {
                "String".output(Time)
            }
        }
        assertTrue { captured.output.contains("String") && captured.output.contains("OutputTest") }
        assertEquals(3, captured.output.lines().size, captured.output)
    }

    @Test
    fun `OutputParameters test`(){
        val nameSpan =  "Name".style(TextStyler.ValueTag.ClassName)
        val params = Output(nameSpan, "Some content".colorize(Colour.Blue).toPair())
        val captured = captureOutput { params.printAll() }
        assertTrue { captured.output.contains("Name") }
        assertTrue { captured.output.contains("Some content") }
        assertTrue { captured.output.contains(Colour.Blue) }
    }

    @Test
    fun `OutputCompare test`(){
        val instance1 = Instance()
        val instance2 = Instance()
        val captured = captureOutput { instance1.outputCompare(instance2) }
        assertTrue { captured.output.contains("Instance") }
        assertTrue { captured.output.contains(instance1.hashCode().toString()) }
        assertTrue { captured.output.contains(Colour.Red) }
    }

    @Test
    fun `Output type list test`(){
        val strings = listOf("String", "Integer", "Boolean")
        val expectedString = "[String, Integer, Boolean]${SpecialChars.NEW_LINE}"
        val captured = captureOutput { strings.output() }
        assertEquals(expectedString,  captured.output)
    }

    @Test
    fun `Output with time parameter`() {
        val captured = captureOutput {
            "String".output(Time)
        }
        assertTrue { captured.output.contains("String") }
    }
}
package po.test.misc.interfaces

import po.misc.data.strings.appendStyledLine
import po.misc.data.strings.contains
import po.misc.data.styles.Colour
import po.misc.interfaces.named.NamedComponent
import po.misc.io.captureOutput
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestNamedComponent : NamedComponent {

    override val name: String = "TestNamedComponent"

    @Test
    fun `Output building`() {
        val captured = captureOutput {
            output { appendLine("String1"); appendLine("String2") }
        }
        val lines = captured.output.lines()
        assertEquals(4, lines.size)
        assertTrue{ lines[0].contains("TestNamedComponent") && lines[0].contains(Colour.Blue)  }
        assertEquals("String1",  lines[1])
        assertEquals("String2",  lines[2])
        assertEquals("",  lines[3])
    }

    @Test
    fun `Output building with colour`() {
        val captured = captureOutput {
            output(Colour.Green){ appendLine("String1"); appendLine("String2") }
        }
        val lines = captured.output.lines()
        assertTrue{ lines[0].contains("TestNamedComponent") && lines[0].contains(Colour.Blue)  }
        assertTrue{ lines[1].contains("String1") && lines[1].contains(Colour.Green)  }
        assertTrue{ lines[2].contains("String2") && lines[2].contains(Colour.Green)  }
    }

    @Test
    fun `Output building coloured lines not overwritten`() {
        val captured = captureOutput {
            output(Colour.Green){
                appendStyledLine("String1", Colour.MagentaBright)
                appendLine("String2")
            }
        }
        val lines = captured.output.lines()
        assertTrue{ lines[0].contains("TestNamedComponent") && lines[0].contains(Colour.Blue)  }
        assertTrue{ lines[1].contains("String1") && lines[1].contains(Colour.MagentaBright)  }
        assertTrue{ lines[2].contains("String2") && lines[2].contains(Colour.Green)  }
    }
}
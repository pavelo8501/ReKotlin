package po.test.misc.data.strings

import po.misc.collections.repeatBuild
import po.misc.data.PrettyPrint
import po.misc.data.strings.stringify
import po.misc.data.strings.stringifyTree
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.debugging.ClassResolver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestStringify {

    private class Report1(val text:String, ){
        val someReport = mutableListOf<Report1>()
        fun add(text: String, times: Int){
            times.repeatBuild {
                someReport.add( Report1(text))
            }
        }
        override fun toString(): String = text
    }
    private class Report2(val text:String): PrettyPrint{
        override val formattedString: String get() = text.colorize(Colour.Blue)
        val subEntries = mutableListOf<Report2>()
        fun add(text: String, repeatTimes: Int):Report2{
            repeatTimes.repeatBuild {
                subEntries.add(Report2("${text}_${it}"))
            }
            return this
        }
        fun add(subReport: Report2):Report2{
            subEntries.add(subReport)
            return this
        }
        override fun toString(): String = text
    }

    @Test
    fun `Stringification of nested classes`(){
        val report = Report1("Initial record")
        report.add("Sub text", 5)
        val result =  report.stringifyTree(Report1::someReport)
        val lines = result.toString().lines()
        assertEquals(6, lines.size)
    }

    @Test
    fun `Stringification of nested classes with formatting`(){
        val report = Report2("Initial record")
        report.add("Sub text", 1)
        val nested = Report2("Nested entry").add("Nested sub entry", 2)
        report.add(nested)
        val result =  report.stringifyTree(Report2::subEntries)
        val lines = result.toString().lines()
        assertEquals(5, lines.size)
    }

    @Test
    fun `Stringification with configuration action`(){
        val list  = 4.repeatBuild {
            Report2("String #${it + 1}")
        }
        val entry1Hash = list[0].hashCode()
        val headerText = "Header for  Stringification with transformation lambda test"
        val result = list.stringify {
            header = headerText
            prefixEach("${ClassResolver.instanceName(it)} ->")
        }
        val lines = result.toString().lines()
        assertEquals(6, lines.size)
        assertEquals("", lines[0])
        assertEquals(headerText, lines[1])
        assertTrue {
            lines[2].contains("String #1") &&
                    lines[2].contains(entry1Hash.toString())
        }
    }
}
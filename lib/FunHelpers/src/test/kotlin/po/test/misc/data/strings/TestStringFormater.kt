package po.test.misc.data.strings

import org.junit.jupiter.api.Test
import po.misc.data.HasText
import po.misc.data.PrettyPrint
import po.misc.data.output.output
import po.misc.data.strings.stringify
import po.misc.data.strings.stringifyThis
import po.misc.data.styles.Colorizer
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestStringFormater {

    val listOfStrings = listOf("String1", "String2")

    internal enum class Enumerator {
        Enm1
    }
    internal enum class ValueEnumerator(override val value: String) : HasText{
        ValueEnm1("Value 1")
    }

    internal class RandomClass(val value: String = "string value"){
        override fun toString(): String  = value
    }

    internal class PrettifiedClass(val value: String = "string value"): PrettyPrint{
        override val formattedString: String = value.colorize(Colour.Yellow)
        override fun toString(): String =  value
    }

    private class RecursiveClass(
        val text: String = "Some text"
    ): PrettyPrint {

        override val formattedString: String
            get() = text.colorize(Colour.Magenta)

       override fun toString(): String = text

       val list = mutableListOf<RecursiveClass>()

        fun addSubClass(text: String):RecursiveClass{
            val new = RecursiveClass(text)
            list.add(new)
            return new
        }

        fun addToList(text: String):RecursiveClass{
            val new = RecursiveClass(text)
            list.add(new)
            return this
        }
    }

    @Test
    fun `Recursive strings by property`(){

        val recursive = RecursiveClass("Initial")
        val level1 =  recursive.addSubClass("Entry 1")

        level1.addToList("Entry 1_1")
        val lastRecordEntry1Text = "Entry 1_2"
        level1.addToList(lastRecordEntry1Text)

        val entry2 = recursive.addSubClass("Entry 2")
        entry2.addToList("Entry 2_1")
        val lastRecordEntry2Text = "Entry 2_2"
        entry2.addToList(lastRecordEntry2Text)

        val result = recursive.stringify(RecursiveClass::list)

        assertEquals(2, result.formatedRecords.size)
        assertNotNull(result.formatedRecords.lastOrNull()){formated->
            assertNotNull(formated.formatedRecords.lastOrNull()){lastEntry->
                assertEquals(lastRecordEntry2Text, lastEntry.text)
                assertTrue {
                    lastEntry.formatedText.contains(Colour.Magenta.code)
                }
            }
        }
        val resultingString = result.joinText()
        resultingString.output()
        val resultingString2 = result.joinFormattedWithIndent("-")
        resultingString2.output()

        val formated = result.joinFormattedWithIndent("-")
        formated.output()

    }

    @Test
    fun `TextColorizer colorize string not breaking previous colorization`(){
        val text = "Some un-colorized part. " + "Coloured inside".colorize(Colour.Magenta) + " Un-colorized tail"
        Colorizer.applyColour(text, Colour.CyanBright).output()
    }

    @Test
    fun `Stringify function correctly operates on different receivers`(){
        val string = "Some text as string"

        val formater = string.stringify(Colour.CyanBright)
        assertEquals(string, formater.toString())
        formater.output()

        val enum = Enumerator.Enm1
        val formatedEnum = enum.stringify(Colour.CyanBright)
        assertEquals(enum.name, formatedEnum.toString())
        formatedEnum.output()

        val valueEnum = ValueEnumerator.ValueEnm1
        val formatedValueEnum = valueEnum.stringify(Colour.CyanBright)
        formatedValueEnum.output()

        val randomClass = RandomClass()
        val formatedRandomClass = randomClass.stringify(Colour.CyanBright)
        formatedRandomClass.output()
        assertTrue { formatedRandomClass.toString().contains(randomClass.value) }
    }

    @Test
    fun `Stringify prefixed function correctly operates on different receivers`(){
        val string = "Some text as string"

        val formater = string.stringify("Prefix", Colour.CyanBright)
        assertTrue {
            formater.toString().contains(string) && formater.toString().contains("Prefix")
        }
        formater.output()
    }

    @Test
    fun `Stringify function with transform lambda correctly operates on different receivers`(){
        val string = "Some text as string"
        val formater = string.stringify(Colour.CyanBright){
            "Additional text $it"
        }
        formater.output()
        assertTrue {
            val textVal = formater.toString()
            textVal.contains(string) &&
                    textVal.contains("Additional text")
        }

        val randomClass = RandomClass()
        val formatedRandomClass = randomClass.stringify(Colour.CyanBright){
            "Random class: $it"
        }
        formatedRandomClass.output()
        assertTrue {
            val textVal = formatedRandomClass.toString()
            textVal.contains(randomClass.toString()) &&
                    textVal.contains("Random class")
        }
    }

    @Test
    fun `Stringify function with transform lambda correctly operates on list type receiver`(){
        val strings = listOf("Some text as string 1", "Some text as string 2")
        val formated = strings.stringify(Colour.CyanBright){
            "Before: $it"
        }
        formated.output()
        assertTrue {
            val stringVal =  formated.toString()
            stringVal.contains(strings[0]) && stringVal.contains("Before") &&
                    stringVal.contains(strings[1])
        }
        val prettyClasses = listOf(PrettifiedClass("PrettifiedClass 1"), PrettifiedClass("PrettifiedClass 2"))
        val formatedPrettifiedClasses =  prettyClasses.stringify(Colour.CyanBright){
            "Pretty: $it"
        }
        formatedPrettifiedClasses.output()
        assertTrue {
            val stringVal =  formatedPrettifiedClasses.toString()
            stringVal.contains(prettyClasses[0].value) && stringVal.contains("Pretty") &&
                    stringVal.contains(prettyClasses[1].value)
        }
    }

    @Test
    fun `Reified Stringify function with transform lambda able to bring typed receiver in transformation lambda`(){

        val random = RandomClass()
        val formattedReified  = random.stringifyThis<RandomClass>(){random->
            "Reified: $random".stringify(Colour.CyanBright)
        }
        formattedReified.output()
        assertTrue {
            formattedReified.toString().contains("Reified") &&  formattedReified.toString().contains(random.value)
        }

    }


}
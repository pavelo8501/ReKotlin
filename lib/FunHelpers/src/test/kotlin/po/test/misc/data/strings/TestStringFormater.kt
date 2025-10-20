package po.test.misc.data.strings

import org.junit.jupiter.api.Test
import po.misc.data.HasValue
import po.misc.data.PrettyPrint
import po.misc.data.helpers.output
import po.misc.data.strings.stringify
import po.misc.data.strings.stringifyThis
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestStringFormater {

    val listOfStrings = listOf("String1", "String2")

    internal enum class Enumerator {
        Enm1
    }
    internal enum class ValueEnumerator(override val value: String) : HasValue{
        ValueEnm1("Value 1")
    }

    internal class RandomClass(val value: String = "string value"){
        override fun toString(): String  = value
    }

    internal class PrettifiedClass(val value: String = "string value"): PrettyPrint{
        override val formattedString: String = value.colorize(Colour.Yellow)
        override fun toString(): String =  value
    }

    @Test
    fun `TextColorizer colorize string not breaking previous colorization`(){
        val text = "Some un-colorized part. " + "Coloured inside".colorize(Colour.Magenta) + " Un-colorized tail"
        Colour.applyColour(text, Colour.CyanBright).output()
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
        assertTrue {
            formatedValueEnum.toString().contains(valueEnum.value)
        }

        val randomClass = RandomClass()
        val formatedRandomClass = randomClass.stringify(Colour.CyanBright)
        formatedRandomClass.output()
        assertTrue { formatedRandomClass.toString().contains(randomClass.value) }

        val prettyClass = PrettifiedClass()
        val formatedPrettifiedClass =  prettyClass.stringify(Colour.CyanBright)
        formatedPrettifiedClass.output()
        assertEquals(prettyClass.formattedString,  formatedPrettifiedClass.formatedString)
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
    fun `Stringify function correctly operates on list types`(){
        val strings = listOf("Some text as string 1", "Some text as string 2")
        val formated = strings.stringify(Colour.CyanBright)
        formated.output()
        assertTrue {
            val stringVal =  formated.toString()
            stringVal.contains(strings[0]) &&
                    stringVal.contains(strings[1])
        }

        val classes = listOf(RandomClass(), RandomClass("Another string"))
        val formatedRandomClasses = classes.stringify(Colour.CyanBright)
        formatedRandomClasses.output()
        assertTrue {
            val stringVal =  formatedRandomClasses.toString()
            stringVal.contains(classes[0].value) &&
                    stringVal.contains(classes[1].value)
        }
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
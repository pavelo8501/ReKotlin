package po.misc.data.strings

import po.misc.context.CTX
import po.misc.data.HasValue
import po.misc.data.PrettyFormatted
import po.misc.data.PrettyPrint
import po.misc.data.TextContaining
import po.misc.data.styles.Colorizer
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.colorize
import po.misc.debugging.ClassResolver
import po.misc.exceptions.throwableToText
import kotlin.collections.drop
import kotlin.collections.first
import kotlin.collections.isNotEmpty
import kotlin.reflect.KClass


sealed class StringFormatter(var string: String){
    open var formatedString: String = string
    internal val subFormatters = mutableListOf<StringFormatter>()
    fun output(){
        println(formatedString)
        subFormatters.forEach {
            it.output()
        }
    }

    fun returnFormated(): String {
        val resultingString = subFormatters.joinToString(prefix = formatedString) {
            it.returnFormated()
        }
        return resultingString
    }

    override fun toString(): String {

        return if (subFormatters.isNotEmpty()) {
            val subResult = subFormatters.joinToString(prefix = string, postfix = SpecialChars.NEW_LINE , separator = SpecialChars.NEW_LINE) {
                it.toString()
            }
            "${string}${SpecialChars.NEW_LINE}${subResult}"
        } else {
            string
        }
    }

    companion object{



        fun formatKnownTypes2(target: Any?): FormatedEntry {
            return if(target != null){
                val targetAsString = target.toString()
                when(target){
                    is KClass<*> -> {
                       val info = ClassResolver.classInfo(target)
                        FormatedEntry(info.simpleName, info.formattedClassName)
                    }
                    is PrettyFormatted -> {
                        FormatedEntry(targetAsString).also {
                            it.overflowPrevention = true
                        }
                    }
                    is PrettyPrint -> FormatedEntry(targetAsString, target.formattedString)
                    is CTX -> FormatedEntry(targetAsString,  target.identifiedByName)
                    is Enum<*> -> {
                        if(target is TextContaining){
                            FormatedEntry(targetAsString, "${target.name}: ${target.asText()}")
                        }else{
                            FormatedEntry(targetAsString)
                        }
                    }
                    is Throwable ->{
                        FormatedEntry(target.message?:"N/A", target.throwableToText())
                    }
                    is String -> FormatedEntry(targetAsString)
                    else -> FormatedEntry(targetAsString)
                }
            }else{
                FormatedEntry("null")
            }
        }

        fun formatKnownTypes(target: Any?): String {
            return when(target){
                is PrettyPrint -> {
                    target.formattedString
                }
                is CTX -> { target.identifiedByName }
                is Enum<*> -> {
                    if(target is TextContaining){
                        "${target.name}: ${target.asText()}"
                    }else{
                        target.name
                    }
                }
                is String -> target
                else -> target.toString()
            }
        }
    }
}


class SimpleFormatter(
    string: String,
    override var formatedString: String = string
): StringFormatter(string) {

    fun addSubStringFormater(stringFormater: StringFormatter): SimpleFormatter {
        subFormatters.add(stringFormater)
        return this
    }
}

class DSLFormatter(string: String): StringFormatter(string){

    private var firstIteration: Boolean = true
    private fun appendLine(receiver: Any,  colour: Colour): DSLFormatter{
        val formatedText = formatKnownTypes(receiver)
        val dsl =  DSLFormatter(formatedText)
        dsl.formatedString = Colorizer.applyColour(formatedText, colour)
        return dsl
    }

    fun Any.stringify(colour: Colour){
        val formater = appendLine(this@stringify, colour)
        if(firstIteration){
            string = formater.string
            formatedString = formater.formatedString
            firstIteration = false
        }else{
            subFormatters.add(formater)
        }
    }
}

@PublishedApi
internal inline fun stringifyInternal(
    receiver: Any,
    colour: Colour?,
    transform: (String)-> String
):SimpleFormatter {

    val lambdaResult = transform(StringFormatter.formatKnownTypes(receiver))
    return colour?.let {
        SimpleFormatter(lambdaResult, Colorizer.applyColour(lambdaResult, it))
    } ?: SimpleFormatter(lambdaResult)
}


@PublishedApi
internal fun stringifyInternal(
    receiver: Any?,
    prefix: String = "",
    colour: Colour?
): FormatedEntry {
    return if (receiver != null) {
        val formated = StringFormatter.formatKnownTypes2(receiver)
        formated.addPrefix(prefix)
        formated.colour(colour)
    } else {
        FormatedEntry("$prefix null")
    }
}


inline fun Any.stringify(
    colour: Colour? = null,
    transform: (String)-> String
):SimpleFormatter = stringifyInternal(this, colour, transform)


@PublishedApi
internal inline fun stringifyListInternal(
    list: List<Any>,
    colour: Colour?,
    transform: (String)-> String
):SimpleFormatter {

    return if(list.isNotEmpty()) {
        val stringFormater =  list.first().stringify(colour, transform)
        list.drop(1).forEach {
            stringFormater.addSubStringFormater(it.stringify(colour, transform))
        }
        stringFormater
    }else{
        SimpleFormatter("empty", "empty")
    }
}






inline fun List<Any>.stringify(
    colour: Colour,
    transform: (String)-> String
):SimpleFormatter {
   return if(isNotEmpty()) {
        val stringFormater =  first().stringify(colour, transform)
        drop(1).forEach {
            stringFormater.addSubStringFormater( it.stringify(colour, transform))
        }
       stringFormater
    }else{
       SimpleFormatter("empty", "empty")
    }
}

fun List<Any>.stringify(
    colour: Colour
):SimpleFormatter{
  return  if(isNotEmpty()){
        val first = first()
        val stringFormater = first.stringify(prefix = "", colour)
        drop(1).forEach {

        }
      SimpleFormatter(stringFormater.text, stringFormater.formatedText)
    }else{
      SimpleFormatter("empty", "empty".colorize(colour))
    }
}

inline fun <reified T: Any> T.stringifyThis(transform: DSLFormatter.(T)-> Unit):DSLFormatter {
    val formater = DSLFormatter(StringFormatter.formatKnownTypes(this))
    formater.transform(this)
    return formater
}
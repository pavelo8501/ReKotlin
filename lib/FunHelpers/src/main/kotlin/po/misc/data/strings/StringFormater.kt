package po.misc.data.strings

import po.misc.context.CTX
import po.misc.data.HasValue
import po.misc.data.PrettyPrint
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.colorize
import kotlin.collections.drop
import kotlin.collections.first
import kotlin.collections.isNotEmpty


sealed class StringFormatter(var string: String){
    open var formatedString: String = string
    internal val subFormatters = mutableListOf<StringFormatter>()

    fun output(){
        println(formatedString)
        subFormatters.forEach {
            it.output()
        }
    }
    override fun toString(): String{
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
        fun formatKnownTypes(target: Any): String {
            return when(target){
                is PrettyPrint -> {
                    target.formattedString
                }
                is CTX -> { target.identifiedByName }
                is Enum<*> -> {
                    if(target is HasValue){
                        "${target.name}: ${target.value}"
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
        dsl.formatedString = Colour.applyColour(formatedText, colour)
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
        SimpleFormatter(lambdaResult, Colour.applyColour(lambdaResult, it))
    } ?: SimpleFormatter(lambdaResult)
}


@PublishedApi
internal fun stringifyInternal(
    receiver: Any,
    prefix: String?,
    colour: Colour?
):SimpleFormatter {

    val prefixToUse = prefix?.let {
        "$it "
    }?:""

    val formated = prefixToUse + StringFormatter.formatKnownTypes(receiver)
    return colour?.let {
        SimpleFormatter(formated, Colour.applyColour(formated, it))
    } ?: SimpleFormatter(formated)
}

fun Any.stringify(
    prefix: String,
    colour: Colour? = null
):SimpleFormatter {

   return stringifyInternal(this, prefix, colour)
}

fun Any.stringify(
    colour: Colour? = null
):SimpleFormatter {
    return stringifyInternal(this, prefix =  null, colour =  colour)
}



fun Any.stringify():SimpleFormatter {
    val formatedText = StringFormatter.formatKnownTypes(this)
    return if (this !is PrettyPrint) {
        SimpleFormatter(formatedText, formatedText)
    } else {
        SimpleFormatter(formatedText, formattedString)
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

@PublishedApi
internal fun stringifyListInternal(
    list: List<Any>,
    colour: Colour?,
):SimpleFormatter {

    return if(list.isNotEmpty()) {
        val stringFormater =  list.first().stringify(colour)
        list.drop(1).forEach {
            stringFormater.addSubStringFormater(it.stringify(colour))
        }
        stringFormater
    }else{
        SimpleFormatter("empty", "empty")
    }
}

fun List<Any>.stringify():SimpleFormatter = stringifyListInternal(this, colour = null)


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
        val stringFormater = first.stringify(colour)
        drop(1).forEach {
            stringFormater.addSubStringFormater( it.stringify(colour) )
        }
        stringFormater
    }else{
      SimpleFormatter("empty", "empty".colorize(colour))
    }
}

inline fun <reified T: Any> T.stringifyThis(transform: DSLFormatter.(T)-> Unit):DSLFormatter {
    val formater = DSLFormatter(StringFormatter.formatKnownTypes(this))
    formater.transform(this)
    return formater
}
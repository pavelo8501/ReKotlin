package po.misc.data.console

import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

interface PrintHelper{
    data class TypedColourRule<T>(
        val condition: (T) -> Boolean,
        val colour: Colour
    )

    data class ColourRule(
        val condition: (Any) -> Boolean,
        val colour: Colour
    )

    fun timestamp(): LocalTime = LocalTime.now()

    val currentTime: String
        get() = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))

    val currentDateTime: String
        get() = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

    val utcTime: String
        get() = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("HH:mm:ss"))

    fun makeIndention(message: String, indentionCount: Int, indentionSymbol: String = " "): String {
        val indent = indentionSymbol.repeat(indentionCount)
        return "$indent $message"
    }

    fun String.withIndention(indentionCount: Int, indentionSymbol: String = " "): String{
        return makeIndention(this, indentionCount, indentionSymbol)
    }

    fun makeOfColour(message: String, color: Colour): String{
        return "${color.colourStr}$message${Colour.RESET.colourStr}"
    }

    fun <T> colourRule(colour: Colour, condition: (T) -> Boolean): TypedColourRule<T> =
        TypedColourRule(condition, colour)

    fun <T : Any> String.makeOfColour(param: T, vararg rules: TypedColourRule<T>): String {
        return rules.firstOrNull { it.condition(param) }?.let {
            "${it.colour.colourStr}$this${Colour.RESET.colourStr}"
        } ?: this
    }

    fun colourRule(colour: Colour, condition: (Any) -> Boolean): ColourRule =
        ColourRule(condition = condition, colour = colour)

    fun  String.makeOfColour(vararg rules: ColourRule): String {
        return rules.firstOrNull { it.condition(it) }?.let {
            "${it.colour.colourStr}$this${Colour.RESET.colourStr}"
        } ?: this
    }

    fun String.colourCondition(vararg rules: () -> Colour?): String {
        return rules.firstNotNullOfOrNull { it() }?.let {
            "${it.colourStr}$this${Colour.RESET.colourStr}"
        } ?: this
    }

    infix fun String.colourOf(color:Colour): String{
       return makeOfColour(this, color)
    }

}
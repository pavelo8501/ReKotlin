package po.misc.data.pretty_print.parts.options

import po.misc.data.PrettyPrint
import po.misc.data.helpers.repeat
import po.misc.data.pretty_print.parts.cells.RenderRecord
import po.misc.data.strings.EditablePair
import po.misc.data.strings.FormattedText
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.StyleCode
import po.misc.data.styles.TextStyler
import po.misc.data.styles.colorize


interface ExtendedString : TextStyler{

    val text:String
    val styleCode: StyleCode?
    val enabled:Boolean

    val displaySize: Int get() {
        if(enabled) return size
        return 0
    }
    val size: Int get() {return text.length}
}

class Border(
    initialString: String,
    override var styleCode: Colour? = null,
    override var enabled:Boolean = true
):ExtendedString{

    constructor(borderChar: Char, colour: Colour? = null, enabled:Boolean = true) : this(borderChar.toString(), colour, enabled)
    var borderString: String =  initialString
        private set

    val isDefault :Boolean get() = borderString == SpecialChars.WHITESPACE

    val borderInfo: String get() = buildString {
        append("Border: '$borderString' ")
        append("Colour: ${styleCode?.name?:"-"} ")
        append("Enabled: $enabled ")
        append("Is Default: $isDefault")
    }

    override val text: String get() = borderString

    fun applyValues(other: Border){
        borderString = other.borderString
        styleCode = other.styleCode
        enabled = other.enabled
    }

    fun toStringNoColour(repeat: Int): String {
        return borderString.repeat(repeat)
    }
    fun toString(times: Int):String{
        return styleCode?.let {
            borderString.repeat(times).colorize(it)
        }?:borderString.repeat(times)
    }

    fun copy(): Border =  Border(borderString, styleCode, enabled)

    override fun toString(): String {
       return styleCode?.let {
            borderString.colorize(it)
        }?:borderString
    }
}

class Separator(
    initialText: String,
    override var styleCode: StyleCode? = null,
    override var enabled:Boolean = true
):ExtendedString{

    constructor(borderChar: Char, colour: Colour? = null, enabled:Boolean = true) : this(borderChar.toString(), colour, enabled)

    override var text: String =  initialText
        private set

    override val size: Int get() = text.length
    override val displaySize: Int get() {
        if(enabled) return size
        return 0
    }

    val info: String get() = buildString {
        append("Text: '$text' ")
        append("Enabled: $enabled ")
    }

    fun applyValues(other: ExtendedString){
        text = other.text
        styleCode = other.styleCode
        enabled = other.enabled
    }

    fun copy(): Separator =  Separator(text, styleCode, enabled)

    fun toStringNoColour(repeat: Int): String {
        return toString().repeat(repeat)
    }
    override fun toString(): String {
       return if(enabled){
           styleCode?.let {
               text.style(it)
           }?:text
        }else{
           SpecialChars.EMPTY
        }
    }

    fun toString(times: Int):String{
       return  if(enabled){
            toString().repeat(times)
        }else{
           SpecialChars.EMPTY
        }
    }
}

data class Borders(
    var leftBorder:  Border = Border(" "),
    var topBorder: Border? = null,
    var bottomBorder: Border? = null,
): PrettyPrint{
    val hasLeftBorder: Boolean get() = leftBorder.isDefault
    val hasTopBorder: Boolean get() = topBorder != null
    val hasBottomBorder: Boolean get() = bottomBorder != null
    val hasBorders: Boolean get() = hasLeftBorder || hasTopBorder || hasBottomBorder

    private val bordersText get() = buildString {
        append("Left border[${leftBorder.borderInfo}]")
        append("Top border[${topBorder?.borderInfo?:"-"}]")
        append("Bottom border[${bottomBorder?.borderInfo?:"-"}]")
    }

    override val formattedString: String get() = bordersText

    private fun createTopBorder(textLength: Int):FormattedText?{
        if(topBorder == null){
            return null
        }
        val borderText = topBorder?.toStringNoColour(textLength)
        val colorized = topBorder?.toString(textLength)
        return FormattedText(borderText, colorized)
    }
    private fun createBottomBorder(textLength: Int): FormattedText?{
        if(bottomBorder == null){
            return null
        }
        val borderText = bottomBorder?.toStringNoColour(textLength)
        val colorized = bottomBorder?.toString(textLength)
        return FormattedText(borderText, colorized)
    }
    fun wrapText(text:String, textLength: Int): FormattedText {
        if(topBorder == null && bottomBorder == null) {
            return FormattedText(text)
        }
        val topString =  createTopBorder(textLength)
        val bottomString =  createBottomBorder(textLength)
        val textBody = FormattedText(text)
        textBody.addNotNull(topString)
        textBody.addNotNull(bottomString)
        return textBody
    }

    override fun toString(): String = "Borders [$bordersText]"

}

data class InnerBorders(
    var leftBorder:  Border = Border(" | ", enabled = false),
    var rightBorder: Border = Border(" | ", enabled = false),
){

    val bordersEnabled :Boolean get() = leftBorder.enabled || rightBorder.enabled
    val bothBordersEnabled: Boolean get() =  leftBorder.enabled && rightBorder.enabled

    val displaySize: Int get() = leftBorder.displaySize + rightBorder.displaySize

    private val onlyLeft: Boolean get() = leftBorder.enabled && !rightBorder.enabled
    private val onlyRight: Boolean get() = !leftBorder.enabled && rightBorder.enabled



    fun disable(){
        leftBorder.enabled = false
        rightBorder.enabled = false
    }

    fun wrapText(text:String): String {
        if(onlyLeft){
           return  "$leftBorder$text"
        }
        if(onlyRight){
            return  "$text$rightBorder"
        }
        if(bothBordersEnabled){
            return "$leftBorder$text$rightBorder"
        }
        return text
    }

    fun wrapText(editablePair: EditablePair) {
        if(onlyLeft){
            editablePair.write("$leftBorder${editablePair.plain}", "$leftBorder${editablePair.formatted}")
        }
        if(onlyRight){
            editablePair.write("${editablePair.plain}$rightBorder", "${editablePair.formatted}$rightBorder")
        }
        if(bothBordersEnabled){
            val plainBordered = "$leftBorder${editablePair.plain}$rightBorder"
            val formattedBordered = "$leftBorder${editablePair.formatted}$rightBorder"
            editablePair.write(plainBordered, formattedBordered)
        }
    }

    fun wrapText(record: RenderRecord) {
        if(onlyLeft){
            val plainBordered = "$leftBorder${record.plain}"
            val formattedBordered = "$leftBorder${record.formatted}"
            record.write(plainBordered, formattedBordered)
            return
        }
        if(onlyRight){
            record.write("${record.plain}$rightBorder", "${record.formatted}$rightBorder")
        }
        if(bothBordersEnabled){
            val plainBordered = "$leftBorder${record.plain}$rightBorder"
            val formattedBordered = "$leftBorder${record.formatted}$rightBorder"
            record.write(plainBordered, formattedBordered)
        }
    }
}


package po.misc.data.pretty_print.parts.cells

import po.misc.data.pretty_print.parts.options.ExtendedString
import po.misc.data.strings.FormattedPair

class RenderRecord(
    override var plain: String,
    override var formatted:String = plain
): FormattedPair {

    constructor( value: FormattedPair):this(value.plain, value.formatted)
    private var  keyValueSeparator: ExtendedString? = null

    var plainValue:String = plain
        private set

    var formattedValue:String = formatted
        private set

    var plainKey:String = ""
        private  set(value) {
            field = value
            plain = "$value${keyValueSeparator?:""}$plain"
        }

    var formattedKey:String = ""
       private set(value) {
            formatted = "${value}$keyValueSeparator$formatted"
            field = value
        }

    val plainKeySize : Int get() = plainKey.length
    val plainValueSize : Int get() = plainValue.length
    override val totalPlainLength: Int get() =  plain.length
    val hasKey: Boolean get() = plainKey.isNotEmpty()

    fun addKey(key:FormattedPair, separator: ExtendedString):RenderRecord{
        keyValueSeparator = separator
        plainKey = key.plain
        formattedKey = key.formatted
        return this
    }

    fun setKey(plainText:String,  formattedText: String){
        plain = plainValue
        formatted = formattedValue
        plainKey = plainText
        formattedKey = formattedText
    }

    fun setValue(plainText:String,  formattedText: String){
        if(hasKey){
            plain = "${plainKey}${keyValueSeparator?:""}$plainText"
            formatted = "${formattedKey}${keyValueSeparator?:""}$formattedText"
        }else{
            plain =  plainText
            formatted = formattedText
        }
        plainValue = plainText
        formattedValue = formattedText
    }

    fun append(plainText:String){
        plain = "${plain}$plainText"
        formatted = "${formatted}$plainText"
    }

    fun prepend(plainText:String){
        plain = "${plainText}$plain"
        formatted = "${plainText}$formatted"
    }

    fun write(plainText: String,  formatedText: String):RenderRecord{
        plain = plainText
        formatted = formatedText
        return this
    }

    override fun toString(): String = formatted
}
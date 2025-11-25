package po.misc.data.strings

import po.misc.data.helpers.orDefault
import po.misc.data.helpers.replaceIfNull
import po.misc.data.styles.Colorizer
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.applyColour


interface FormattedPair{
    val text: String
    val formatedText: String

}

class FormatedEntry(
    initialText: String,
    initialFormatedText: String = initialText
): FormattedPair {

    constructor(
        listToFlatten : List<FormatedEntry>
    ):this(listToFlatten.firstOrNull()?.text?:"", listToFlatten.firstOrNull()?.formatedText?:""){
        if(listToFlatten.size > 1){
            listToFlatten.drop(1).forEach {
                formatedRecords.add(it)
            }
        }
    }

    var recursionLevel: Int = 0
        internal set

    override var formatedText: String = initialFormatedText
        private set

    override var text: String = initialText
        private set

    internal val formatedRecords = mutableListOf<FormatedEntry>()
    val formatedString: String get() = joinFormated()


    internal fun addPrefix(prefix: String?): FormatedEntry{
        val effectivePrefix = prefix.orDefault("")
        text = "$effectivePrefix$text"
        formatedText = "$effectivePrefix$formatedText"
        return this
    }

    fun addFormated(formated: FormatedEntry):FormatedEntry{
        formated.recursionLevel = this.recursionLevel + 1
        formatedRecords.add(formated)
        return this
    }

    fun addText(text: String, formatedText: String = text):FormatedEntry{
        val entry  = FormatedEntry(text, formatedText)
        entry.recursionLevel = this.recursionLevel + 1
        formatedRecords.add(entry)
        return this
    }

    fun colour(colour: Colour?): FormatedEntry{
        if(colour != null){
            formatedText =  Colorizer.colour(formatedText, colour)
        }
        return this
    }

    fun applyColour(colour: Colour?): FormatedEntry{
        if(colour != null){
            formatedText = formatedText.applyColour(colour)
        }
        return this
    }

    fun applyIndention(
        indentionOffset: Int,
        indentionString : String,
        indentionChar: Char? = null,
        indentUnformatted: Boolean = false
    ): FormatedEntry{

        val indentPref = indentionChar?.toString()?:""

        val indentionString = indentionString.repeat(indentionOffset)
        formatedText = "$indentionString$indentPref$formatedText"

        if(indentUnformatted){
            text = "$indentionString$indentPref$text"
        }
        return this
    }

    fun joinFormated(listSeparator: String = SpecialChars.WHITESPACE): String {
        val resultingText = if(recursionLevel == 0){
            "$formatedText$listSeparator"
        }else{
            "$formatedText$listSeparator"
        }
        val resultingString = formatedRecords.joinToString(prefix =  resultingText, separator = listSeparator) { formated->
            if(formated.formatedRecords.isNotEmpty()){
                formated.joinFormated(listSeparator)
            }else{
                formated.formatedText
            }
        }
        return resultingString
    }

    fun joinFormated(listDirection: ListDirection): String {
        var listSeparator = SpecialChars.NEW_LINE
        val resultingText =  when(listDirection){
            is Horizontal ->{
                listSeparator = listDirection.separator
                if(recursionLevel == 0){
                    "$formatedText${listDirection.separator}"
                }else{
                    "$formatedText$listDirection.separator"
                }
            }
            is Vertical->{
                listSeparator = SpecialChars.NEW_LINE
                "$formatedText${SpecialChars.NEW_LINE}"
            }
        }
        val resultingString = formatedRecords.joinToString(prefix =  resultingText, separator = listSeparator) { formated->
            if(formated.formatedRecords.isNotEmpty()){
                formated.joinFormated(listSeparator)
            }else{
                formated.formatedText
            }
        }
        return resultingString
    }

    fun returnFormated(): String = joinFormated()

    fun joinFormattedWithIndent(
        indentionString: String,
        listSeparator: String = SpecialChars.WHITESPACE,
    ): String {
        val indentText = indentionString.repeat(recursionLevel)
        val resultingText =  if(recursionLevel == 0){
            formatedText
        }else{
            "${SpecialChars.NEW_LINE}$indentText$formatedText"
        }
        val resultingString = formatedRecords.joinToString(prefix =  resultingText, separator = listSeparator) {formated->
            formated.joinFormattedWithIndent(indentionString, listSeparator)
        }
        return resultingString
    }

    fun joinText(listSeparator: String = SpecialChars.WHITESPACE): String {
        val resultingText =  if(recursionLevel == 0){
            formatedText
        }else{
            "${SpecialChars.NEW_LINE}$text"
        }
        val resultingString = formatedRecords.joinToString(prefix = resultingText, separator = listSeparator) {formated->
            formated.joinText(listSeparator)
        }
        return resultingString
    }

    fun textJoinWithIndent(
        indentionString: String,
        listSeparator: String = SpecialChars.WHITESPACE,
    ): String {
        val indentText = indentionString.repeat(recursionLevel)
        val resultingText =  if(recursionLevel == 0){
            formatedText
        }else {
            "${SpecialChars.NEW_LINE}$indentText$text"
        }
        val resultingString = formatedRecords.joinToString(prefix = resultingText, separator = listSeparator) {formated->
            formated.textJoinWithIndent(indentionString, listSeparator)
        }
        return resultingString
    }

    override fun toString(): String {
        return text
    }
}
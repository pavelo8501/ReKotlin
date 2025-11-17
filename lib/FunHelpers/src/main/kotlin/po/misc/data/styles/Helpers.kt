package po.misc.data.styles

interface StylableText{

    fun String.newLine(): String{
       return "${this}${SpecialChars.NEW_LINE}"
    }
}

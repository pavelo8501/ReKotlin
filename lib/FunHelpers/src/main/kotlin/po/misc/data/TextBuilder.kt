package po.misc.data

import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.StringFormatter
import po.misc.data.styles.TextStyler
import po.misc.data.styles.colorize


interface TextBuilder: TextStyler{

    fun String.concat(colour: Colour? = null, lineBuilder: ()-> String): String{
        return if(colour != null){
            this.colorize(colour) +  lineBuilder()
        }else{
            this +  lineBuilder()
        }
    }
    fun String.newLine(colour: Colour? = null, lineBuilder: ()-> String): String{
        return if(colour != null){
            this.colorize(colour) + SpecialChars.NEW_LINE + lineBuilder()
        }else{
            this +  SpecialChars.NEW_LINE + lineBuilder()
        }
    }
     fun Any.toFormatted(): String{
        return formatKnownTypes(this).formatted
    }

    companion object:  StringFormatter()

}
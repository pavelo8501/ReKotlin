package po.misc.data

import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.colorize

interface TextBuilder {




    fun String.concat(colour: Colour? = null, lineBuilder: ()-> String): String{
        return if(colour != null){
            this.colorize(colour) +  lineBuilder()
        }else{
            this +  lineBuilder()
        }
    }

    fun String.newLine(colour: Colour? = null, lineBuilder: ()-> String): String{
        return if(colour != null){
            this.colorize(colour) + SpecialChars.newLine + lineBuilder()
        }else{
            this +  SpecialChars.newLine + lineBuilder()
        }
    }

}
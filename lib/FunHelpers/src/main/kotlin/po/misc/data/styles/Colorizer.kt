package po.misc.data.styles


import po.misc.data.styles.Colour.RESET

interface Colorizer {

    fun applyColour(text: String, colour: Colour): String = Companion.applyColour(text, colour)
    fun applyColour(text: String, bgColour: BGColour): String = Companion.applyColour(text, bgColour )
    fun applyColour(text: String, colour: Colour, bgColour: BGColour): String = Companion.applyColour(text, ColourPallet(colour, bgColour))

    fun colour(text: String, colour: Colour): String = Companion.colour(text, colour)
    fun colour(text: String, bgColour: BGColour): String = Companion.colour(text, bgColour)
    fun colour(text: String, colour: Colour, bgColour: BGColour): String = Companion.colour(text, ColourPallet(colour, bgColour))

    fun colour(text:String, style: ColourStyle): String = Companion.colour(text, style)

    companion object : StyleHelper(){

        fun isStyled(text:String):Boolean = isTextStyled(text)
        fun applyColour(text: String, colour: StyleCode): String {
            val segments = extractColorSegments(text)
            val result = StringBuilder()
            var lastIndex = 0
            segments.forEach { segment ->
                if (segment.start > lastIndex) {
                    val before = text.substring(lastIndex, segment.start)
                    if(!isStyled(before)){
                        val stripped = stripAnsi(before)
                        val reColorized = colour(stripped, colour)
                        result.append(reColorized)
                    }
                }
                val subtext = text.substring(segment.start, segment.end)
                result.append(subtext)
                lastIndex = segment.end
            }
            if (lastIndex < text.length) {
                val tail = text.substring(lastIndex)
                if(!isStyled(tail)){
                    val strippedTail = stripAnsi(tail)
                    val reColorizedTail = colour(strippedTail, colour)
                    result.append(reColorizedTail)
                }
            }
            return result.toString()
        }
        fun applyColour(text: String, colour: Colour, bgColour: BGColour?, reapply: Boolean): String {
            if(isStyled(text)) {
                if(!reapply){
                    return text
                }
                val segments = extractColorSegments(text)
                val result = StringBuilder()
                var lastIndex = 0
                segments.forEach { segment ->
                    if (segment.start > lastIndex) {
                        val before = text.substring(lastIndex, segment.start)
                        if(bgColour != null){
                            result.append(colour(before, colour, bgColour))
                        }else{
                            result.append(colour(before, colour))
                        }
                    }
                    result.append(text.substring(segment.start, segment.end))
                    lastIndex = segment.end
                }
                if (lastIndex < text.length) {
                    val tail = text.substring(lastIndex)
                    if(bgColour != null){
                        result.append(colour(tail, colour, bgColour))
                    }else{
                        result.append(colour(tail, colour))
                    }
                }
                return result.toString()
            }else{
               return  colour(text, colour, bgColour?: BGColour.Default)
            }
        }
        fun applyColour(text: String, colour: Colour, bgColour: BGColour?): String = applyColour(text, colour, bgColour, true)
        fun colour(text: String, bgColour: BGColour): String {
            val stripped = stripAnsiIfAny(text)
            return if(bgColour.ordinal == 0){
                stripped
            }else{
                "${bgColour.code}${stripped}${BGColour.RESET.code}"
            }
        }
        fun colour(text: String, colour: Colour, bgColour: BGColour): String {
            val stripped = stripAnsiIfAny(text)
            val codesApplied = bgColour.ordinal + colour.ordinal
            return if(codesApplied ==0){
                text
            }else{
                "${bgColour.code}${colour.code}${stripped}${BGColour.RESET.code}"
            }
        }
        fun colour(text:String, style: StyleCode): String{
            val stripped = stripAnsiIfAny(text)
            val codesApplied = style.ordinal
            return if(codesApplied == 0){
                text
            }else{
                "${style.code}${stripped}${RESET.code}"
            }
        }
    }
}

fun Colour.colorize(text: String): String = Colorizer.colour(text, this)


package po.misc.data.styles


enum class Colour(val code: String) {
    Default(""),
    Red("\u001B[31m"),
    Yellow("\u001B[33m"),
    Green("\u001B[32m"),
    Gray("\u001B[90m"),
    Blue("\u001B[34m"),
    Magenta("\u001B[35m"),
    Cyan("\u001B[36m"),
    White("\u001B[37m"),
    BlackBright("\u001B[90m"),
    RedBright("\u001B[91m"),
    GreenBright("\u001B[92m"),
    YellowBright("\u001B[93m"),
    BlueBright("\u001B[94m"),
    MagentaBright("\u001B[95m"),
    CyanBright("\u001B[96m"),
    WhiteBright("\u001B[97m"),
    RESET("\u001B[0m");

    companion object {

        data class AnsiColorSegment(
            val start: Int,    // index of color code
            val end: Int,      // index where it is reset
            val code: String   // the actual ANSI start code like \u001B[31m
        )

        val ANSI_COLOR_REGEX: Regex = Regex("\\u001B\\[(?!0m)[0-9;]*m")
        val ANSI_START_REGEX: Regex = Regex("\u001B\\[(?!0m)[0-9;]*m")
        val ANSI_RESET_REGEX: Regex = Regex("\u001B\\[0m")

        private fun extractColorSegments(text: String): List<AnsiColorSegment> {
            val segments = mutableListOf<AnsiColorSegment>()
            val starts = ANSI_START_REGEX.findAll(text).toList()
            val resets = ANSI_RESET_REGEX.findAll(text).toList()
            for (startMatch in starts) {
                val resetMatch = resets.firstOrNull { it.range.first > startMatch.range.first }
                if (resetMatch != null) {
                    segments.add(
                        AnsiColorSegment(
                            start = startMatch.range.first,
                            end = resetMatch.range.last + 1,
                            code = startMatch.value
                        )
                    )
                } else {
                    segments.add(
                        AnsiColorSegment(
                            start = startMatch.range.first,
                            end = text.length,
                            code = startMatch.value
                        )
                    )
                }
            }
            return segments
        }

        fun fromValue(colourStr: String): Colour {
            entries.firstOrNull { it.code == colourStr }?.let {
                return it
            }
            return RESET
        }

        fun colour(text: String, color: Colour): String{
            return "${color.code}$text${RESET.code}"
        }

        fun applyColour(text: String, colour: Colour?): String{
            if(colour != null){
                val segments = extractColorSegments(text)
                return if(segments.isEmpty()){
                    Colour.colour(text, colour)
                }else {
                    val result = StringBuilder()
                    var lastIndex = 0
                    segments.forEach { segment ->
                        if (segment.start > lastIndex) {
                            val before = text.substring(lastIndex, segment.start)
                            result.append(Colour.colour(before, colour))
                        }
                        result.append(text.substring(segment.start, segment.end))
                        lastIndex = segment.end
                    }
                    if (lastIndex < text.length) {
                        val tail = text.substring(lastIndex)
                        result.append(Colour.colour(tail, colour))
                    }
                    result.toString()
                }
            }else{
                return text
            }
        }


        fun makeOfColour(color: Colour, text: String): String {
            return if (text.contains("\u001B[")) {
                text
            } else {
                "${color.code}$text${RESET.code}"
            }
        }
    }
}
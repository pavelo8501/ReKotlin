package po.misc.data.strings

import po.misc.data.styles.Colour


data class AnsiColorSegment(
    val start: Int,    // index of color code
    val end: Int,      // index where it is reset
    val code: String   // the actual ANSI start code like \u001B[31m
)

private val ANSI_COLOR_REGEX = Regex("\\u001B\\[(?!0m)[0-9;]*m")

private val ANSI_START_REGEX = Regex("\u001B\\[(?!0m)[0-9;]*m")
private val ANSI_RESET_REGEX = Regex("\u001B\\[0m")

internal fun extractColorSegments(text: String): List<AnsiColorSegment> {
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

@PublishedApi
internal fun textColorizer(text: String, colour: Colour?): String{
    if(colour != null){
        val segments = extractColorSegments(text)
        return if(segments.isEmpty()){
            Colour.fontColour(text, colour)
        }else {
            val result = StringBuilder()
            var lastIndex = 0
            segments.forEach { segment ->
                if (segment.start > lastIndex) {
                    val before = text.substring(lastIndex, segment.start)
                    result.append(Colour.fontColour(before, colour))
                }
                result.append(text.substring(segment.start, segment.end))
                lastIndex = segment.end
            }
            if (lastIndex < text.length) {
                val tail = text.substring(lastIndex)
                result.append(Colour.fontColour(tail, colour))
            }
            result.toString()
        }
    }else{
        return text
    }
}
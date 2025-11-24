package po.misc.data.styles

import po.misc.data.styles.Colour.RESET

interface Colorizer {

    fun applyColour(text: String, colour: Colour): String = Companion.applyColour(text, colour = colour )
    fun colour(text: String, colour: Colour): String = Companion.colour(text, colour)

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

        fun applyColour(text: String, colour: Colour, applyIfColorized: Boolean): String {
            val segments = extractColorSegments(text)
            if (segments.isEmpty()) {
                return colour(text, colour)
            } else {
                return if (applyIfColorized) {
                    val result = StringBuilder()
                    var lastIndex = 0
                    segments.forEach { segment ->
                        if (segment.start > lastIndex) {
                            val before = text.substring(lastIndex, segment.start)
                            result.append(colour(before, colour))
                        }
                        result.append(text.substring(segment.start, segment.end))
                        lastIndex = segment.end
                    }
                    if (lastIndex < text.length) {
                        val tail = text.substring(lastIndex)
                        result.append(colour(tail, colour))
                    }
                    result.toString()
                } else {
                    text
                }
            }
        }
        fun applyColour(text: String, colour: Colour): String = applyColour(text, colour, true)

        fun applyColour(text: String, bgColour: BGColour, applyIfColorized: Boolean): String {
            val segments = extractColorSegments(text)
            if (segments.isEmpty()) {
                return colour(text, bgColour)
            } else {
                return if (applyIfColorized) {
                    val result = StringBuilder()
                    var lastIndex = 0
                    segments.forEach { segment ->
                        if (segment.start > lastIndex) {
                            val before = text.substring(lastIndex, segment.start)
                            result.append(colour(before, bgColour))
                        }
                        result.append(text.substring(segment.start, segment.end))
                        lastIndex = segment.end
                    }
                    if (lastIndex < text.length) {
                        val tail = text.substring(lastIndex)
                        result.append(colour(tail, bgColour))
                    }
                    result.toString()
                } else {
                    text
                }
            }
        }
        fun applyColour(text: String, bgColour: BGColour): String = applyColour(text, bgColour, true)


        fun applyColour(text: String, colour: Colour, bgColour: BGColour?, applyIfColorized: Boolean): String {
            val segments = extractColorSegments(text)
            if (segments.isEmpty()) {
                return  if(bgColour != null){
                    colour(text, colour, bgColour)
                }else{
                    colour(text, colour)
                }
            } else {
                return if (applyIfColorized) {
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
                    result.toString()
                } else {
                    text
                }
            }
        }
        
        fun applyColour(text: String, colour: Colour, bgColour: BGColour?): String = applyColour(text, colour, bgColour, true)


        fun colour(text: String, colour: Colour): String {
            return "${colour.code}$text${RESET.code}"
        }
        fun colour(text: String, bgColour: BGColour): String {
            return "${bgColour.code}$text${BGColour.RESET.code}"
        }
        fun colour(text: String, colour: Colour, bgColour: BGColour): String {
            return "${bgColour.code}${colour.code}$text${BGColour.RESET.code}"
        }
    }
}

fun Colour.colorize(text: String): String = Colorizer.colour(text, this)


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

        private fun makeOfColour(bgColour: BGColour, text: String): String {
            return "${bgColour.code}$text${BGColour.RESET.code}"
        }

        private fun makeOfColour(bgColour: BGColour, colour: Colour,  text: String): String {
            return "${bgColour.code}${colour.code}$text${BGColour.RESET.code}"
        }

        fun applyColour(text: String, colour: Colour): String {
            val segments = extractColorSegments(text)
            return if (segments.isEmpty()) {
                colour(text, colour)
            } else {
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
            }
        }

        fun colour(text: String, colour: Colour): String {
            return "${colour.code}$text${RESET.code}"
        }
        fun colour(text: String, colour: Colour, bgColour: BGColour?): String {
            return if(bgColour != null){
                makeOfColour(bgColour, colour, text)
            }else{
                colour(text, colour)
            }
        }
        fun colour(text: String, bgColour: BGColour): String {
            return makeOfColour(bgColour, text)
        }

        fun applyColour(bgColour: BGColour, text: String): String {
            val segments = extractColorSegments(text)
            return if (segments.isEmpty()) {
                makeOfColour(bgColour, text)
            } else {
                val result = StringBuilder()
                var lastIndex = 0
                segments.forEach { segment ->
                    if (segment.start > lastIndex) {
                        val before = text.substring(lastIndex, segment.start)
                        result.append(makeOfColour(bgColour, before))
                    }
                    result.append(text.substring(segment.start, segment.end))
                    lastIndex = segment.end
                }
                if (lastIndex < text.length) {
                    val tail = text.substring(lastIndex)
                    result.append(makeOfColour(bgColour, tail))
                }
                result.toString()
            }
        }
    }
}

fun Colour.colorize(text: String): String = Colorizer.colour(text, this)


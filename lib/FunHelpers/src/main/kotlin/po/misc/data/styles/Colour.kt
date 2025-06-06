package po.misc.data.styles

enum class Colour(val colourStr: String) {
    RED("\u001B[31m"),
    YELLOW("\u001B[33m"),
    GREEN("\u001B[32m"),
    BLUE("\u001B[34m"),
    MAGENTA("\u001B[35m"),
    CYAN("\u001B[36m"),
    WHITE("\u001B[37m"),
    RESET("\u001B[0m"),
    BRIGHT_BLACK("\u001B[90m"),
    BRIGHT_RED("\u001B[91m"),
    BRIGHT_GREEN("\u001B[92m"),
    BRIGHT_YELLOW("\u001B[93m"),
    BRIGHT_BLUE("\u001B[94m"),
    BRIGHT_MAGENTA("\u001B[95m"),
    BRIGHT_CYAN("\u001B[96m"),
    BRIGHT_WHITE("\u001B[97m"),

    RED_BG("\u001B[41m"),
    GREEN_BG("\u001B[42m"),
    YELLOW_BG("\u001B[43m"),
    BLUE_BG("\u001B[44m"),
    MAGENTA_BG("\u001B[45m"),
    CYAN_BG("\u001B[46m"),
    WHITE_BG("\u001B[47m");


    companion object {
        fun fromValue(colourStr: String): Colour {
            entries.firstOrNull { it.colourStr == colourStr }?.let {
                return it
            }
            return RESET
        }
        fun makeOfColour(color: Colour,  text: String): String {
            return if (text.contains("\u001B[")) {
                text
            } else {
                "${color.colourStr}$text${RESET.colourStr}"
            }
        }
    }
}
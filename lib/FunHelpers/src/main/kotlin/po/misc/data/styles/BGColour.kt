package po.misc.data.styles


enum class BGColour(val code: String) {

    Red("\u001B[41m"),
    Green("\u001B[42m"),
    Yellow("\u001B[43m"),
    Blue("\u001B[44m"),
    Magenta("\u001B[45m"),
    Cyan("\u001B[46m"),
    White("\u001B[47m"),
    RESET("\u001B[0m");


    companion object {

        fun fromValue(colourStr: String): BGColour {
            entries.firstOrNull { it.code == colourStr }?.let {
                return it
            }
            return RESET
        }
        fun makeOfColour(bgColour: BGColour, text: String): String {
            return if (text.contains("\u001B[")) {
                text
            } else {
                "${bgColour.code}$text${RESET.code}"
            }
        }

        fun makeOfColour(bgColour: BGColour, colour: Colour, text: String): String {
            return if (text.contains("\u001B[")) {
                text
            } else {
                "${bgColour.code}${colour.code}$text${RESET.code}"
            }
        }
    }
}
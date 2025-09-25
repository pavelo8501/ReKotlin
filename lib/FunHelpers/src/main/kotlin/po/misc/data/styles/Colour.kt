package po.misc.data.styles

enum class Colour(val code: String) {
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
        fun fromValue(colourStr: String): Colour {
            entries.firstOrNull { it.code == colourStr }?.let {
                return it
            }
            return RESET
        }
        fun makeOfColour(color: Colour,  text: String): String {
            return if (text.contains("\u001B[")) {
                text
            } else {
                "${color.code}$text${RESET.code}"
            }
        }
    }
}

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
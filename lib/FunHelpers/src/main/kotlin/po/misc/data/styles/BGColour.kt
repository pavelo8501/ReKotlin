package po.misc.data.styles

enum class BGColour(val code: String) {
    NONE(""),
    Red("\u001B[41m"),
    Green("\u001B[42m"),
    Yellow("\u001B[43m"),
    Blue("\u001B[44m"),
    Magenta("\u001B[45m"),
    Cyan("\u001B[46m"),
    White("\u001B[47m"),
    RESET("\u001B[0m");

}
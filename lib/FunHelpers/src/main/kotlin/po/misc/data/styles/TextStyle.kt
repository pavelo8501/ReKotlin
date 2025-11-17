package po.misc.data.styles


enum class TextStyle (val code: String) {
    BOLD("\u001B[1m"),
    ITALIC("\u001B[3m"),
    UNDERLINE("\u001B[4m"),
    DIM("\u001B[2m"),
    RESET("\u001B[0m");
}
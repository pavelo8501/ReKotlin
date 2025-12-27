package po.misc.data.styles



enum class TextStyle (override val code: String) : StyleCode {
    Regular(""),
    Bold("\u001B[1m"),
    Italic("\u001B[3m"),
    Underline("\u001B[4m"),
    Dim("\u001B[2m"),
    Reset("\u001B[0m");
}
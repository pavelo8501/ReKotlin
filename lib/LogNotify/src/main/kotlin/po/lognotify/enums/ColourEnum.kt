package po.lognotify.enums


enum class ColourEnum(val colourStr: String) {
    RED("\u001B[31m"),
    YELLOW("\u001B[33m"),
    BLUE("\u001B[34m"),
    MAGENTA("\u001B[35m"),
    RESET("\u001B[0m");


    companion object {
        fun fromValue(colourStr: String): ColourEnum? {
            entries.firstOrNull { it.colourStr == colourStr }?.let {
                return it
            }
            return RESET
        }
    }
}
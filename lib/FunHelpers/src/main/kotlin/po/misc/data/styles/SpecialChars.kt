package po.misc.data.styles

enum class SpecialChars(val char: String) {
    NONE(""),
    NewLine("\n");

    override fun toString(): String{
        return char
    }
}
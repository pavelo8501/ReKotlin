package po.misc.data.styles

enum class SpecialChars(val char: String) {
    Empty(""),
    NewLine("\n");

    override fun toString(): String{
        return char
    }
}
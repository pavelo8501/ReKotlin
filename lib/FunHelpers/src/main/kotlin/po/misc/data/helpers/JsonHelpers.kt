package po.misc.data.helpers

fun String.jsonEscape(): String {
    val sb = StringBuilder(this.length)
    for (c in this) {
        when (c) {
            '\\' -> sb.append("\\\\") // escape backslash
            '\"' -> sb.append("\\\"") // escape double quote
            '\b' -> sb.append("\\b")  // escape backspace
            '\u000C' -> sb.append("\\f") // escape formfeed
            '\n' -> sb.append("\\n") // escape newline
            '\r' -> sb.append("\\r") // escape carriage return
            '\t' -> sb.append("\\t") // escape tab
            else -> {
                if (c < ' ') {
                    // escape other control characters as unicode
                    sb.append(String.format("\\u%04x", c.code))
                } else {
                    sb.append(c)
                }
            }
        }
    }
    return sb.toString()
}
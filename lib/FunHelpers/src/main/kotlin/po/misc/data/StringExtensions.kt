package po.misc.data

import po.misc.data.styles.SpecialChars


fun String.linesCount(): Int{
    val thisString = this
    return  thisString.split(SpecialChars.NEW_LINE).count()
}

fun String.splitLines(): List<String> {
    val thisString = this
    val result = thisString.split(SpecialChars.NEW_LINE)
    return  result
}

fun String.count(substring: String): Int {
    val thisString = this
    val splits =  thisString.split(substring)
    val spaces = splits.size - 1
    return  spaces.coerceAtLeast(0)
}



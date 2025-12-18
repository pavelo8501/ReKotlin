package po.misc.data


val String.linesCount: Int get() {
    return lines().size
}


fun String.substringCount(substring: String): Int {
    val thisString = this
    val splits =  thisString.split(substring)
    val spaces = splits.size - 1
    return  spaces.coerceAtLeast(0)
}


fun String.containsAnyOf(vararg substrings: String): Boolean {
    for (substring in substrings) {
        if(substringCount(substring) > 0){
            return true
        }
    }
    return false
}





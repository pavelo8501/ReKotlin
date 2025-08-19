package po.misc.data.tags

interface Tagged<E: Enum<E>> {
    val enumTag: EnumTag<E>
    val alias: String
}

data class EnumTag<E: Enum<E>>(
    val value: E,
    val tagClass: Class<E>
){
    var alias: String = ""
}
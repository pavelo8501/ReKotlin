package po.restwraptor.extensions


fun String?.stripBearer(): String{
    return this?.removePrefix("Bearer")?.trim() ?: ""
}

fun String?.asBearer(): String{
    return "Bearer ${this.stripBearer()}"
}
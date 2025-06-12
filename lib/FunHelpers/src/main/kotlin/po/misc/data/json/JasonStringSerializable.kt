package po.misc.data.json

import po.misc.data.PrintableBase
import po.misc.data.interfaces.Printable



interface JasonStringSerializable  {


    fun toJson(): String

}


private fun formatJsonValue(v: String): String {
    return if (v.matches(Regex("-?\\d+(\\.\\d+)?")) || v == "true" || v == "false") {
        v
    } else {
        "\"$v\""
    }
}

fun toJsonLike(value: Any): String{
    val prefixRemoved = value.toString().substringAfter("(").substringBeforeLast(")")
    val entries = prefixRemoved.split(", ")
    val json = entries.joinToString(", ") {
        val (k, v) = it.split("=")
        "\"$k\": ${formatJsonValue(v)}"
    }
    return "{$json}"
}

fun numberFormat(number: Number): String{
  return  number.toString()
}

fun stringFormat(string: String): String{
    return "\"${string.replace("\"", "\\\"")}\""
}

fun jasonStringSerializable(value: JasonStringSerializable): String{
   return toJsonLike(value)
}


fun formatJsonSafe(value: Any?): String {
    return when (value) {
        null -> "null"
        is Number, is Boolean -> value.toString()
        is String -> "\"${value.replace("\"", "\\\"")}\""
        is JasonStringSerializable -> toJsonLike(value)
        else -> "\"${value.toString().replace("\"", "\\\"")}\""
    }
}

class ComposableProvider<R: Any>(
    private vararg val providers: SerializationProvider<R>
): SerializationProvider<R> {
    override fun serialize(value: R): String {
        return providers.fold(value.toString()) { acc, provider ->
            @Suppress("UNCHECKED_CAST")
            provider.serialize(acc as R)
        }
    }
}

object StringDefaultProvider : SerializationProvider<String> {
    override fun serialize(value: String): String = "\"${value.replace("\"", "\\\"")}\""
}

object IntDefaultProvider : SerializationProvider<Int> {
    override fun serialize(value: Int): String = numberFormat(value)
}


object TrimmedQuotedStringProvider : SerializationProvider<String> {
    override fun serialize(value: String): String = "\"${value.trim()}\""
}

object UppercaseProvider : SerializationProvider<String> {
    override fun serialize(value: String): String = "\"${value.uppercase()}\""
}

object NanoTimeProvider : SerializationProvider<Long> {
    override fun serialize(value: Long): String = "${value}ms"
}

object ElapsedTimeProvider : SerializationProvider<Float> {
    override fun serialize(value: Float): String = "${value}ms"
}



//fun<T: Printable> PrintableBase<T>.toJsonLikeImpl(
//propertyMap: Map<String, KPropertyRecord1<T, *>>,
//indent: Int = 0
//): String {
//    val pad = "  ".repeat(indent)
//    val childPad = "  ".repeat(indent + 1)
//
//    val props = propertyMap.map { (key, record) ->
//        try {
//            val value = record.property.get(self)
//            "\"$key\": ${formatJsonSafe(value)}"
//        } catch (ex: Throwable) {
//            "\"$key\": \"<error>\""
//        }
//    }
//
//    val childrenJson = children.takeIf { it.isNotEmpty() }?.let {
//        val nested = it.joinToString(",\n") { child ->
//            (child as? PrintableBase<T>)?.toJsonLikeImpl(propertyMap, indent + 2)
//                ?: "\"<non-printable-child>\""
//        }
//        "$childPad\"children\": [\n$nested\n$childPad]"
//    }
//
//    val jsonContent = (props + listOfNotNull(childrenJson)).joinToString(",\n$childPad")
//    return "$pad{\n$childPad$jsonContent\n$pad}"
//}
//
//inline fun <reified T : Printable> PrintableBase<T>.toJsonLike(indent: Int = 0): String {
//    val propertyMap = toPropertyMap1<T, Any>()
//    return this.toJsonLikeImpl(propertyMap, indent)
//}
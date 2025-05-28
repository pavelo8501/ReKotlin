package po.misc.reflection.properties

data class MappingCheckResult(
    val propertyName: String,
    val source: PropertyRecord<*, *>,
    val target: PropertyRecord<*, *>?,
    val status: CheckStatus,
    val message: String
)

enum class CheckStatus {
    PASSED, WARNING, FAILED
}
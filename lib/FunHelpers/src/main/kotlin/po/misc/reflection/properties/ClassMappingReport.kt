package po.misc.reflection.properties

import po.misc.collections.CompositeKey
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased

data class ClassMappingReport(
    val component: Identifiable,
    val from: ValueBased,
    val to: ValueBased,
    val results: List<MappingCheckResult>
) {
    fun hasFailures(): Boolean = results.any { it.status == CheckStatus.FAILED }
    fun printReport(): String = buildString {
        appendLine("Mapping Report [${component.qualifiedName}] $from → $to")
        results.forEach { r ->
            appendLine(" - ${r.propertyName}: ${r.status} → ${r.message}")
        }
    }
}
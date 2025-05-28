package po.misc.reflection.properties

import po.misc.collections.CompositeKey
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased


class MappingValidator(private val propertyMap: Map<ValueBased, Map<String, PropertyRecord<*, Any?>>>) {

    fun checkMapping(component: Identifiable, from: ValueBased, to: ValueBased): ClassMappingReport {
        val fromMap = propertyMap[from] ?: emptyMap()
        val toMap = propertyMap[to] ?: emptyMap()

        val results = fromMap.map { (name, sourceRecord) ->
            val targetRecord = toMap[name]
            when {
                targetRecord == null -> MappingCheckResult(
                    name, sourceRecord, null, CheckStatus.FAILED,
                    "Missing in $to"
                )
                sourceRecord.property.returnType != targetRecord.property.returnType -> MappingCheckResult(
                    name, sourceRecord, targetRecord, CheckStatus.FAILED,
                    "Type mismatch: ${sourceRecord.property.returnType} vs ${targetRecord.property.returnType}"
                )
                else -> MappingCheckResult(
                    name, sourceRecord, targetRecord, CheckStatus.PASSED,
                    "OK"
                )
            }
        }

        return ClassMappingReport(component, from, to, results)
    }

    fun checkAllMappings(
        component: Identifiable,
        vararg mappings: Pair<ValueBased, ValueBased>
    ): List<ClassMappingReport> {
        return mappings.map { (from, to) -> checkMapping(component, from, to) }
    }
}
package po.misc.reflection.properties

import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased
import po.misc.reflection.properties.models.MappingCheck


class MappingValidator(private val propertyMap: Map<ValueBased, Map<String, PropertyRecord<*, Any?>>>) {

    fun checkMapping(mapping  : MappingCheck): ClassMappingReport {
        val fromMap = propertyMap[mapping.from] ?: emptyMap()
        val toMap = propertyMap[mapping.to] ?: emptyMap()

        val results = fromMap.map { (name, sourceRecord) ->
            val targetRecord = toMap[name]
            when {
                targetRecord == null -> MappingCheckResult(
                    name, sourceRecord, null, CheckStatus.FAILED,
                    "Missing in ${mapping.to}"
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
        return ClassMappingReport(mapping.component, mapping.from, mapping.to, results)
    }

    fun checkMappings(vararg mapping : MappingCheck): Map<MappingCheck, ClassMappingReport>{
       return mapping.associate {
           it to checkMapping(it)
       }
    }


    fun checkAllMappings(
        component: Identifiable,
        vararg mappings: Pair<ValueBased, ValueBased>
    ): List<ClassMappingReport> {
        return mappings.map { (from, to) -> checkMapping(MappingCheck(component, from, to)) }
    }
}
package po.exposify.dto.configuration

enum class ValidationCheck(val value: String) {
    ForeignKeys("Validating foreign keys"),
    MandatoryProperties("Validating mandatory properties"),
    AttachedForeign("Attached foreign initialized"),
    ParentReferences("Parent references")
}
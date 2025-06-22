package po.exposify.dto.configuration

enum class ValidationCheck(val value: String) {
    MandatoryProperties("Validating mandatory properties"),
    ForeignKeys("Validating foreign keys"),
    AttachedForeign("Attached foreign initialized")
}
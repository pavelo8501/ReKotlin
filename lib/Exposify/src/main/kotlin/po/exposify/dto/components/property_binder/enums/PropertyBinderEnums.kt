package po.exposify.dto.components.property_binder.enums

enum class UpdateMode{
    ENTITY_TO_MODEL,
    ENTITY_TO_MODEL_FORCED,
    MODEL_TO_ENTITY,
    MODEL_TO_ENTITY_FORCED,
}

enum class PropertyType{
    READONLY,
    TWO_WAY,
    SERIALIZED
}
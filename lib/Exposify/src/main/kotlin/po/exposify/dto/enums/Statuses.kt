package po.exposify.dto.enums


enum class DTOClassStatus {
    Uninitialized,
    PreFlightCheck,
    Initialized,
}

enum class DTOInitStatus {
    UNINITIALIZED,
    PARTIAL_WITH_DATA,
    INITIALIZED,
    INIT_FAILURE
}
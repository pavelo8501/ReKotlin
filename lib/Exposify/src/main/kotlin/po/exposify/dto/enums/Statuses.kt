package po.exposify.dto.enums


enum class DTOClassStatus {
    Undefined,
    PreFlightCheck,
    Live
}


enum class DTOInitStatus {
    UNINITIALIZED,
    PARTIAL_WITH_DATA,
    INITIALIZED,
    INIT_FAILURE
}
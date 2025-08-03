package po.exposify.dto.enums

enum class DTOClassStatus {
    Uninitialized,
    PreFlightCheck,
    Initialized,
}

enum class DTOStatus {
    Uninitialized,
    PartialWithData,
    PartialWithEntity,
    Complete,
    Cached,
}

enum class DataStatus {
    New,
    Dirty,
    UpToDate,
    PreflightCheckMock,
}

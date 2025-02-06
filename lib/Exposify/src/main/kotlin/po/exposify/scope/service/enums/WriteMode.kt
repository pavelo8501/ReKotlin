package po.exposify.scope.service.enums

/**
 * Defines write mode to the database.
 * Where :
 * STRICT will cancel whole transaction returning error if any of the child EntityDTOs are not written
 * RELAXED will proceed even if child write failed. Information about error writes can be extracted from DTOContext
 */
enum class WriteMode {
    STRICT,
    RELAXED
}
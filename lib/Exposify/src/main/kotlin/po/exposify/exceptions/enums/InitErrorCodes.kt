package po.exposify.exceptions.enums


enum class ExceptionCode(val value: Int) {

    // Generic / Undefined
    UNDEFINED(0),

    // Initialization & Lazy-loading Issues
    INITIALIZATION_OUTSIDE_CONTEXT(1001),
    NOT_INITIALIZED(1002),
    ALREADY_EXISTS(1003),
    INVALID_DATA(1004),
    KEY_NOT_FOUND(1005),
    LAZY_NOT_INITIALIZED(1006),
    CAST_FAILURE(1007),
    NO_EMPTY_CONSTRUCTOR(1008),
    CONSTRUCTOR_MISSING(1009),
    REFLECTION_ERROR(1010),
    VALUE_NOT_FOUND(1011),
    VALUE_IS_NULL(1012),

    // Database Layer
    DB_TABLE_CREATION_FAILURE(2001),
    DB_CRUD_FAILURE(2002),
    DB_NO_TRANSACTION_IN_CONTEXT(2003),
    QueryResolvedNull(2004),

    ABNORMAL_STATE(3000),
    BAD_DTO_SETUP(3001),

    // Repository.kt & Runtime Execution
    REPOSITORY_RETHROWN(4000),
    REPOSITORY_NOT_INITIALIZED(4001),
    REPOSITORY_NOT_FOUND(4002),
    DTO_LOOKUP_FAILURE(4003),

    // Binding / Data Model Layer
    BINDING_PROPERTY_MISSING(5001),
    REQUESTED_RESULT_MISMATCH(5002),

    // Factory & DTO
    FACTORY_CREATE_FAILURE(6001),

    //Internal Errors and misuse
    METHOD_MISUSED(7001);

    companion object {
        fun getByValue(value: Int): ExceptionCode {
            ExceptionCode.entries.firstOrNull { it.value == value }?.let {
                return it
            }
            return UNDEFINED
        }
    }
}


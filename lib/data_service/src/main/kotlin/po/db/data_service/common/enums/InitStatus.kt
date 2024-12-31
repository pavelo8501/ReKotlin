package po.db.data_service.common.enums

enum class InitStatus (val msg : String = "") {
    UNINITIALIZED(""),
    PARTIAL_DTO("DtoInitialized"),
    PARTIAL_CHILD("ChildInitialized"),
    COMPLETE("WithDaoEntity"),
    INIT_FAILED("Failed")
}




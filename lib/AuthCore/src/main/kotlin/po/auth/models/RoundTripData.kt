package po.auth.models


enum class  SessionOrigin {
    ReCreated,
    Persisted
}

data class RoundTripData(
    val count: Int,
    var origin: SessionOrigin = SessionOrigin.Persisted,
    val auxMessage : String = ""
)

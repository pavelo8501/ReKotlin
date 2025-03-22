package po.auth.sessions

enum class SessionDataType {
    SESSION,       // persistent across the session
    ROUND_TRIP,    // removed after next outbound call or manual flush
    EXTERNAL       // points to unmanaged context or framework-scoped value
}
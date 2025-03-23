package po.auth.sessions.enumerators

enum class SessionDataType(int: Int) {
    SESSION(0),       // persistent across the session
    ROUND_TRIP(1),    // removed after next outbound call or manual flush
    EXTERNAL(2),       // points to unmanaged context or framework-scoped value
}
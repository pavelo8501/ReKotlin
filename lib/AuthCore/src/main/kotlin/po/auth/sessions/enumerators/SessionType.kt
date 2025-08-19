package po.auth.sessions.enumerators

enum class SessionType(val sessionName: String) {
    USER_AUTHENTICATED("AuthenticatedSession"),
    ANONYMOUS("AnonymousSession")
}
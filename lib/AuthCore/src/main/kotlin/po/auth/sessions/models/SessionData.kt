package po.auth.sessions.models

interface SessionKeyData {
    var sessionName: String
    var startTime: String
    var endTime: String
    var userId: Long
    var login: String
    var email: String
    var userGroupId: Long
}
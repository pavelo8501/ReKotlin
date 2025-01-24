package po.restwraptor.interfaces

interface SecuredUser {
    val username: String
    val roles: List<String>

    fun toPayload(): String
}
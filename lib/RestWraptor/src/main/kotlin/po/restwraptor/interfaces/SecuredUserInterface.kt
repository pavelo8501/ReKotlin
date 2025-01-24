package po.restwraptor.interfaces

interface SecuredUserInterface {
    val username: String
    val roles: List<String>

    fun toPayload(): String
}
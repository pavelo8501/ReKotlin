package po.restwraptor.enums

import po.restwraptor.enums.RouteSelector.UNDEFINED

enum class WraptorHeaders(val value: String) {
    UNDEFINED(""),
    Auth("Authorization"),
    XAuthToken("X-Auth-Token");


    override fun toString(): String {
        return value
    }

    companion object {
        fun fromValue(value: String): WraptorHeaders {
            WraptorHeaders.entries.firstOrNull { it.value.lowercase() == value.lowercase() }?.let {
                return it
            }
            return UNDEFINED
        }
    }

}
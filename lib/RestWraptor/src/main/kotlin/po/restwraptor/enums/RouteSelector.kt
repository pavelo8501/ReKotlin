package po.restwraptor.enums

enum class RouteSelector(val value: String) {

    UNDEFINED(""),
    OPTIONS("OPTIONS"),
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    PATCH("PATCH");


    companion object {
        fun fromValue(value: String): RouteSelector {
            RouteSelector.entries.firstOrNull { it.value.lowercase() == value.lowercase() }?.let {
                return it
            }
            return UNDEFINED
        }
    }
}
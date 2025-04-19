package po.restwraptor.extensions

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import po.restwraptor.models.response.ApiResponse

inline fun <reified T : Any> decodeApiResponse(text: String): ApiResponse<T>? {

    val jsonFormatter = Json{
        ignoreUnknownKeys = true
        isLenient = true
    }

    val generic = jsonFormatter.decodeFromString<ApiResponse<JsonElement>>(text)
    return if (!generic.status) {
        ApiResponse<T>().apply {
            status = false
            message = generic.message
            errorCode = generic.errorCode
        }
    } else {
        val typedData = jsonFormatter.decodeFromJsonElement<T>(generic.data!!)
        ApiResponse(typedData)
    }
}
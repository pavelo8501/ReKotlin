package po.restwraptor.interfaces

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

interface SecuredUserInterface {
    var id : Long
    var login: String
    var roles: List<String>

    fun toPayload(): String


    companion object{
        val json = Json{
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        fun toJsonElement(jsonStr : String): JsonElement?{
            try {
                val element = json.parseToJsonElement(jsonStr)
                return element
            }catch (ex: SerializationException){
                throw ex
            }
        }

        fun getValueFromJsonElement(element: JsonElement, key: String): String?{
            element.jsonObject.keys.firstOrNull { it == key }?.let {
               return element.jsonObject[it]?.jsonPrimitive?.content
            }?:run {
                return null
            }
        }

        fun fromPayload(payload: String):SecuredUserInterface? {
            toJsonElement(payload)?.let { element ->

                val result = object : SecuredUserInterface {
                    override var id: Long = 0
                    override var login: String = ""
                    override var roles: List<String> = emptyList()
                    override fun toPayload(): String {
                        return payload
                    }
                }
                result.id = getValueFromJsonElement(element, "id")?.toLong() ?: 0
                result.login = getValueFromJsonElement(element, "login")?:""
                return result
            }
            return null
        }
    }
}
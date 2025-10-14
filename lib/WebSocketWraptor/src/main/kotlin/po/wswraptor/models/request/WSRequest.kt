package po.wswraptor.models.request

import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer


@Serializable
enum class ApiRequestAction(val value: Int){
    UNKNOWN(0),
    CREATE(1),
    UPDATE(2),
    SELECT(3),
    DELETE(4)
}

interface WSMessageBase<P: Any?>{
  val payload: P?
  val resource: String
  val action:  ApiRequestAction
}

@Serializable
data class WSRequest<P: Any>(
    override val payload:P,
    override val resource: String,
    override val action: ApiRequestAction,
): WSMessage<P>("Request"), WSMessageBase<P>{



    fun <T: Any>getSerializer(payloadSerializer : KSerializer<T>) = Companion.getSerializer(payloadSerializer)

    companion object {
        fun <T: Any>getSerializer(payloadSerializer : KSerializer<T>): KSerializer<WSRequest<T>>{
            return WSRequest.serializer(payloadSerializer)
        }
//        inline fun <reified T : WSRequest<Any>> createFactory(
//
//        ): SerializationFactory<T> {
//            return SerializationFactory(T::class)
//        }
    }
}


@Serializable
abstract class WSMessage<P: Any>(private val name : String): WSMessageBase<P>{


}





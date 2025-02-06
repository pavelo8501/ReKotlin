package po.wswraptor.serializers

import kotlinx.serialization.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.descriptors.*
import po.wswraptor.models.request.ApiRequestAction
import po.wswraptor.models.request.WSRequest

class WSRequestSerializer<P: Any>(
    private val payloadSerializer: KSerializer<P>
) : KSerializer<WSRequest<P>> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("WSRequest") {
        element("payload", payloadSerializer.descriptor)
        element<String>("resource")
        element<String>("action")
    }

    override fun serialize(encoder: Encoder, value: WSRequest<P>) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, payloadSerializer, value.payload)
            encodeStringElement(descriptor, 1, value.resource)
            encodeSerializableElement(descriptor, 2, ApiRequestAction.serializer(), value.action)
        }
    }

    override fun deserialize(decoder: Decoder): WSRequest<P> {
        return decoder.decodeStructure(descriptor) {
            var payload: P? = null
            var resource: String? = null
            var action: ApiRequestAction? = null

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> payload = decodeSerializableElement(descriptor, 0, payloadSerializer)
                    1 -> resource = decodeStringElement(descriptor, 1)
                    2 -> action = decodeSerializableElement(descriptor, 2, ApiRequestAction.serializer())
                    CompositeDecoder.DECODE_DONE -> break
                    else -> throw SerializationException("Unexpected index: $index")
                }
            }
            WSRequest(
                payload = payload ?: throw SerializationException("Missing payload"),
                resource = resource ?: throw SerializationException("Missing resource"),
                action = action ?: throw SerializationException("Missing action")
            )
        }
    }
}
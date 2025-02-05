package po.wswraptor.test.components.serializationfactory

import io.ktor.util.reflect.typeInfo
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import po.wswraptor.components.serializationfactory.SerializationFactory
import po.wswraptor.models.request.ApiRequestAction
import po.wswraptor.models.request.WSRequest
import po.wswraptor.models.response.WSResponse
import po.wswraptor.test.common.Test1
import po.wswraptor.test.common.Test2
import po.wswraptor.test.common.Test3
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestSerializationFactory {

    companion object {

        lateinit var inboundFactory: SerializationFactory<WSRequest<Any>>
        private  lateinit var  request1 : WSRequest<Test1>
        private  lateinit var  request2 : WSRequest<Test2>
        private  lateinit var  request3 : WSRequest<Test3>

        lateinit var outboundFactory : SerializationFactory<WSResponse<Any>>
        private  lateinit var  response1 : WSResponse<Test1>
        private  lateinit var  response2 : WSResponse<Test2>
        private  lateinit var  response3 : WSResponse<Test3>


        @BeforeAll
        @JvmStatic
        fun setup() {
            inboundFactory = WSRequest.Companion.createFactory()
            outboundFactory = WSResponse.Companion.createFactory()

            request1 = WSRequest<Test1>(Test1(1, "Test1Name"), "Test1", ApiRequestAction.CREATE)
            request2 = WSRequest<Test2>(Test2(1, 12), "Test2", ApiRequestAction.SELECT)
            request3 = WSRequest<Test3>(Test3(1, 121, "Test3Name", true), "Test3", ApiRequestAction.SELECT)

            response1 = WSResponse<Test1>(Test1(1, "Test1Name"), "Test1", ApiRequestAction.SELECT)
            response2 = WSResponse<Test2>(Test2(1, 12), "Test2", ApiRequestAction.DELETE)
            response3 = WSResponse<Test3>(Test3(1, 12, "Test3Name", true), "Test3", ApiRequestAction.DELETE)
        }
    }

    @OptIn(InternalSerializationApi::class)
    @Test
    fun `test if serializers properly extracted`(){
        inboundFactory.registerPayload<Test1>("Test1")
        inboundFactory.registerPayload<Test2>("Test2")
        inboundFactory.registerPayload<Test3>("Test3")
        assertEquals(3, inboundFactory.repository.count())
        assertEquals("Test1", inboundFactory.repository["Test1"]!!.routName)
        assertEquals("Test2", inboundFactory.repository["Test2"]!!.routName)
        assertEquals("Test3", inboundFactory.repository["Test3"]!!.routName)
    }

    @Test
    fun `test if can serialize deserialize`(){
        inboundFactory.registerPayload<Test1>("Test1")
        inboundFactory.registerPayload<Test2>("Test2")
        inboundFactory.registerPayload<Test3>("Test3")

        val serializedString =  inboundFactory.serialize<WSRequest<Any>>(typeInfo<WSRequest<Any>>(), request3)
        assertNotNull(serializedString)
        serializedString.let {
            assertDoesNotThrow { JsonPrimitive(it) }
            assertTrue(it.contains("Test3"))
            assertTrue(it.contains("Test3Name"))
            assertTrue(it.contains("121"))
        }

        inboundFactory.deserialize<Test3>(serializedString).let {
            assertNotNull(it)
            assertTrue { it.action == ApiRequestAction.SELECT }
            assertTrue { it.resource == "Test3" }
            assertEquals(request3.payload, it.payload)
        }
    }
}
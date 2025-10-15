package po.test.misc.io

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import po.misc.io.WriteOptions
import po.misc.io.writeSourced
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class TestFileWriter {

    private val json = Json{
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = false
    }

    @Serializable
    internal data class DataPayload(
        val category: String = "Temp",
        val parameter1: String = "Parameter1",
    ){
        @SerialName("creationTime")
        val creationTime: String = Instant.now().toString()
    }

   private val overwriteOption = WriteOptions(overwriteExistent = true)

    @Test
    fun `Sourced file writer by ByteArray return type`(){
        val payload = DataPayload()
        val file = payload.writeSourced("temp_files/1.json", overwriteOption){
            json.encodeToString(payload).toByteArray()
        }
        assertEquals("Temp", file.source.category)

        assertTrue {
            with(file){
                lastModified.elapsedSince(file.source.creationTime.asInstant()) < 3.seconds
            }
        }
    }

    @Test
    fun `Sourced file writer by String return type`(){
        val payload = DataPayload()
        val sourced = payload.writeSourced("temp_files/1.json", Charsets.UTF_8, overwriteOption){
            json.encodeToString(payload)
        }
        assertEquals("Temp", sourced.source.category)
    }



}
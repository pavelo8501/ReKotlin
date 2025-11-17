package po.test.misc.io

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import po.misc.io.readSourced
import po.misc.io.readToString
import java.time.Instant
import kotlin.test.assertEquals


class TestFileReaders {

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

    @Test
    fun `Sourced file with String parameter`(){
        val sourced = readSourced("temp_files/1.json", Charsets.UTF_8){
            json.decodeFromString<DataPayload>(it)
        }
        assertEquals("Temp", sourced.source.category)
    }

    @Test
    fun `Sourced file with ByteArray parameter`(){
        val sourced = readSourced("temp_files/1.json"){
            json.decodeFromString<DataPayload>(it.readToString())
        }
        assertEquals("Temp", sourced.source.category)
    }

    @Test
    fun `Sourced file meta data loaded correctly`(){
        val sourced = readSourced("temp_files/1.json", Charsets.UTF_8){
            json.decodeFromString<DataPayload>(it)
        }
        assertEquals("image.json", sourced.fileName)
    }

}
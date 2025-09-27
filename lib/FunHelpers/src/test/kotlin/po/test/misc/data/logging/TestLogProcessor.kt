package po.test.misc.data.logging

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import po.misc.data.logging.LogEmitter
import po.misc.data.logging.processor.LogProcessor
import po.misc.data.processors.SeverityLevel
import po.misc.exceptions.managedException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class TestLogProcessor: LogEmitter {

    val processor = LogProcessor(this)

    enum class TestLogErrorCode{
        Code1,
        Code2
    }

    @BeforeEach
    fun clear(){
        processor.clearData()
    }

    @Test
    fun `Log processor stores data`(){
        processor.info("Some msg")
        processor.warn("Some warning")
        assertEquals(2, processor.records.size)
    }

    @Test
    fun `Exceptions are properly logged`(){
        val genericException = Exception("Error Message")
        processor.log(genericException)

        val storedData = assertNotNull(processor.activeRecord)
        assertEquals(SeverityLevel.EXCEPTION, storedData.severity)
    }

    @Test
    fun `Complete information extracted from trace exceptions`(){
        val managed = managedException("Error Message", TestLogErrorCode.Code2)
        processor.log(managed)
        val storedData = assertNotNull(processor.activeRecord)
        assertEquals(SeverityLevel.EXCEPTION, storedData.severity)
    }



}
package po.api.rest_service

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import po.api.rest_service.logger.*
import java.time.LocalDateTime


class LoggingServiceTest {

    @Test
    fun `Test log level enums are correctly comparable`(){
        val logLevel1 = LogLevel.WARNING
        val logLevel2 = LogLevel.WARNING

        var compareByLevel = false
        if(logLevel1.level == logLevel2.level){
            compareByLevel = true
        }
        assertTrue(compareByLevel)

        var compareByEntry = false
        if(logLevel1 == logLevel2){
            compareByEntry = true
        }
        assertTrue(compareByLevel)

    }

    @Test
    fun `log function is called for appropriate LogLevel or higher`() = runBlocking {
        val loggingService = LoggingService()

        val logCalled = CompletableDeferred<Boolean>()
        val logFunction: LogFunction = { _, logLevel, _, _ ->
            if (logLevel == LogLevel.WARNING) {
                logCalled.complete(true)
            } else {
                logCalled.complete(false)
            }
        }
        loggingService.registerLogFunction(LogLevel.WARNING, logFunction)
        loggingService.warn("This is a warning")
        assertTrue(logCalled.await(), "The log function should have been called for LogLevel.WARNING")

        logCalled.complete(false)
        loggingService.error("This is an error")
        assertTrue(logCalled.await(), "The log function should have been called higher LogLevel.ERROR")
    }

    @Test
    fun logFunctionIsNotCalledForLowerLogLevel() = runBlocking {
        val loggingService = LoggingService()
        var logCalled = false
        val logFunction: LogFunction = { _, level, _, _ ->
            if (level == LogLevel.EXCEPTION) logCalled = true
        }
        loggingService.registerLogFunction(LogLevel.EXCEPTION, logFunction)
        loggingService.warn("This is a warning")
        assertFalse(logCalled)
    }

    @Test
    fun logFunctionHandlesExceptionGracefully() = runBlocking {
        val loggingService = LoggingService()
        val logFunction: LogFunction = { _, _, _, _ ->
            throw RuntimeException("Logging failed")
        }
        loggingService.registerLogFunction(LogLevel.MESSAGE, logFunction)
        assertDoesNotThrow {
            loggingService.info("This is an info message")
        }
    }

    @Test
    fun clearLogFunctionsRemovesAllLogFunctions() = runBlocking {
        val loggingService = LoggingService()
        var logCalled = false
        val logFunction: LogFunction = { _, _, _, _ ->
            logCalled = true
        }
        loggingService.registerLogFunction(LogLevel.MESSAGE, logFunction)
        loggingService.clearLogFunctions()
        loggingService.info("This is an info message")
        assertFalse(logCalled)
    }

}
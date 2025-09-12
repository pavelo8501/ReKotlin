package po.test.misc.functions

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import po.misc.exceptions.ManagedException
import po.misc.functions.repeater.models.RepeaterConfig
import po.misc.functions.repeater.repeatOnFault
import po.misc.time.ExecutionTimeStamp
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TestRepeatOnException {

//    internal fun doSomething(doSomethingsCount: Int, exception: Throwable? = null): Int {
//        exception?.let { throw it }
//        val nextCount = doSomethingsCount + 1
//        println("DoSomething#$nextCount")
//        return nextCount
//    }

//    @Test
//    fun `repeatOnException does not repeat on success`() {
//        val projectedRepeats = 2
//        var actualRepeats = 0
//        var repeatCount = 0
//
//        val actualCount = repeatOnFault({
//            setMaxAttempts(projectedRepeats)
//        }) {
//            actualRepeats++
//
//            repeatCount = if (actualRepeats == 2) {
//                doSomething(repeatCount, Exception("Some exception"))
//            } else {
//                doSomething(repeatCount)
//            }
//            repeatCount
//        }
//        assertEquals(1, actualCount)
//    }
//
//    @Test
//    fun `repeatOnException repeats on exceptions`() {
//        val projectedRepeats = 2
//        var actualRepeats = 0
//        var repeatCount = 0
//
//        assertThrows<Exception> {
//            repeatCount = repeatOnFault({
//                setMaxAttempts(projectedRepeats)
//            }) {
//                actualRepeats++
//                repeatCount = doSomething(repeatCount, Exception("Some exception"))
//                repeatCount
//            }
//        }
//        assertEquals(projectedRepeats, actualRepeats, "Repetitions count mismatch")
//    }
//
//
//    @Test
//    fun `repeatOnException onException function stores repacked exception`() {
//        val projectedRepeats = 2
//        var actualRepeats = 0
//        var repeatCount = 0
//
//        var lastException: Throwable? = null
//        var exceptions: List<Throwable> = listOf()
//
//        val repeatConfig = RepeaterConfig(projectedRepeats) { stats ->
//            exceptions = stats.exceptions
//            val managed = ManagedException("Repacked", null, stats.exception)
//            lastException = managed
//            managed
//        }
//
//        assertThrows<Throwable> {
//            repeatCount = repeatOnFault( {repeatConfig} ) {
//                actualRepeats++
//                if (lastException != null) {
//                    doSomething(repeatCount, lastException)
//                } else {
//                    doSomething(repeatCount, Exception("Some exception"))
//                }
//                repeatCount
//            }
//        }
//        assertEquals(projectedRepeats, actualRepeats, "Repetitions count mismatch")
//
//        assertEquals(2, exceptions.size)
//        assertIs<Exception>(exceptions[0])
//        assertIs<ManagedException>(exceptions[1])
//    }
//
//    @Test
//    fun `repeatOnException onException function holds thread`() {
//        val projectedRepeats = 2
//        var actualRepeats = 0
//        var repeatCount = 0
//
//
//        val repeatConfig = RepeaterConfig(projectedRepeats) { stats ->
//            Thread.sleep(stats.attempt * 10L)
//        }
//        val timestamp = ExecutionTimeStamp("SleepTest", "0")
//        timestamp.stopTimer()
//        assertThrows<Throwable> {
//            repeatCount = repeatOnFault({repeatConfig}) {
//                actualRepeats++
//                doSomething(repeatCount, Exception("Some exception"))
//                repeatCount
//            }
//        }
//        timestamp.stopTimer()
//        assertTrue(timestamp.elapsed > 30)
//        assertEquals(projectedRepeats, actualRepeats, "Repetitions count mismatch")
//    }

}
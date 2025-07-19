package po.test.misc.functions

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import po.misc.functions.repeatIfFaulty
import kotlin.test.assertEquals

class TestFunctionHelpers {


    companion object{
        var topActionCount = 0
    }

    fun action(inputParam: Int, exception: Throwable?): Int{
        if(exception != null){
            throw exception
        }
        return inputParam
    }

    fun <R: Any?> topAction(block:()->R):R{

        topActionCount++

      //  exception?.let { throw it }

        return  block.invoke()
    }

    fun `Test repeatIfFaulty2`(){
        val plannedRepeatCount = 4
        var actualRepeatCount = 0

      val finalResult =  repeatIfFaulty<Int>(plannedRepeatCount) { attempt->
            actualRepeatCount++
            val exception = Exception("Generic exception")
            if(actualRepeatCount < 4){
                action(10,exception)
            }else{
                action(100,null)
            }
        }
        assertEquals(plannedRepeatCount, actualRepeatCount, "Repeat count discrepancy")
        assertEquals(100, finalResult)
    }


    fun `repeatIfFaulty2 exits early if OK`(){
        val plannedRepeatCount = 1
        var actualRepeatCount = 0

        val finalResult =  repeatIfFaulty<Int>(plannedRepeatCount) { attempt->
            actualRepeatCount++
            val exception = Exception("Generic exception")
            if(actualRepeatCount > 4){
                action(10,exception)
            }else{
                action(300,null)
            }
        }
        assertEquals(plannedRepeatCount, actualRepeatCount, "Repeat count discrepancy")
        assertEquals(300, finalResult)
    }


    fun `RepeatIfFaulty block repeats exactly given times and results in exception`(){
        val plannedRepeatCount = 4
        var actualRepeatCount = 0

        assertThrows<Exception>{
            repeatIfFaulty(times = plannedRepeatCount){attempt->
                actualRepeatCount++
                action(10, Exception("Generic exception"))
            }
        }
        assertEquals(plannedRepeatCount, actualRepeatCount, "Repeat count discrepancy")
    }


    @Test
    fun `RepeatIfFaulty block exits with result on success`(){
        val plannedRepeatCount = 4
        val expectedResult = 10
        var actualRepeatCount = 0

       val result =  assertDoesNotThrow{

           repeatIfFaulty(times = plannedRepeatCount){data->
                 actualRepeatCount++
                if(data.currentAttempt < 4){
                    action(0, Exception("Generic exception"))
                }else{
                    action(expectedResult, null)
                }
            }
        }
        assertEquals(plannedRepeatCount, actualRepeatCount, "Repeat count discrepancy")
        assertEquals(expectedResult, result, "Wrong result returned")
    }

//    @Test
//    fun `RepeatIfFaulty block exits immediately on times = 0`(){
//        val plannedRepeatCount = 1
//        var actualRepeatCount = 0
//        val expectedResult = 20
//        val result = repeatIfFaulty(times = 0, {}) { attempt ->
//            actualRepeatCount++
//            action(expectedResult, null)
//        }
//        assertEquals(plannedRepeatCount, actualRepeatCount, "Repeat count mismatch")
//        assertEquals(expectedResult, result.result, "Wrong result returned")
//    }
//
//    @Test
//    fun `RepeatIfFaulty block exits immediately on times = 1`(){
//        val plannedRepeatCount = 1
//        var actualRepeatCount = 0
//        val expectedResult = 20
//        val result = repeatIfFaulty(times = -10, {}) { attempt ->
//            actualRepeatCount++
//            action(expectedResult, null)
//        }
//        assertEquals(plannedRepeatCount, actualRepeatCount, "Repeat count mismatch")
//        assertEquals(expectedResult, result.result, "Wrong result returned")
//    }
}
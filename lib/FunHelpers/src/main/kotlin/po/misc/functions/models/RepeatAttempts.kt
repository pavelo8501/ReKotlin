package po.misc.functions.models



class RepeatAttempts(
    val attemptsTotal: Int = 0,
){

    var currentAttempt: Int = 1
        private set

    var attemptsLeft: Int = 0
        private set

    val failDetected: Boolean get(){
        return currentAttempt != 0
    }


    fun recalculate(attempt: Int, attemptsTotal: Int):RepeatAttempts{
        attemptsLeft  = attemptsTotal - attempt
        return this
    }

    override fun toString(): String {
        return """
           RepeatAttempts[
             Fail Detected = $failDetected;
             Attempts in total = $attemptsTotal;
             This attempt = $currentAttempt;
             Attempts Left = $attemptsLeft;
           ]""".trimIndent()
    }
}
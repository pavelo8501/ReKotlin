package po.misc.data.logging.procedural

import po.misc.data.HasValue


enum class StepTolerance { STRICT, ALLOW_NULL, ALLOW_FALSE, ALLOW_EMPTY_LIST }


enum class StepResult(override val value: String):HasValue{
    InProgress("In Progress"),
    OK("OK"),
    Fail("Failure"),
}
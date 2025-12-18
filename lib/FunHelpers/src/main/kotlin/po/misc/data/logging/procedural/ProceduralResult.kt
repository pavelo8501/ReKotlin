package po.misc.data.logging.procedural

import po.misc.data.PrettyPrint
import po.misc.data.logging.Loggable
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.colorize

enum class StepTolerance { STRICT, ALLOW_NULL, ALLOW_FALSE, ALLOW_EMPTY_LIST }

enum class ProceduralResult(){
    Ok,
    Fail,
    Warning
}

sealed interface StepResult: PrettyPrint{

    val text: String
    val ok: Boolean

    class OK: StepResult{
        override val ok: Boolean = true
        override val text: String = "OK"
        override val formattedString: String get() = text.colorize(Colour.Green)
    }

    class Fail(
        val reason: String? = null,
    ): StepResult{
        override val ok: Boolean = false
        override val text: String = "Fail"
        override val formattedString: String get() = text.colorize(Colour.Red)
    }

    class Warning(val warnings: List<Loggable>): StepResult{

        private val warningText : String get() {
           return if(warnings.size > 1){
                warnings.joinToString(separator = SpecialChars.NEW_LINE) {
                    it.formattedString
                }
            }else{
                warnings.firstOrNull()?.formattedString?:""
            }
        }


        override val ok: Boolean = false
        override val text: String = "Warning [ $warningText ]"
        override val formattedString: String get() = "Warning[${warnings.size}]".colorize(Colour.YellowBright)
        override fun toString(): String = "StepResult<Warning>"

    }

}




package po.misc.data.logging.procedural

import po.misc.data.PrettyPrint
import po.misc.data.styles.Colour


data class ProceduralEntry(
    val stepName: String,
    val stepBadge: String,
    val stepResult: String,
    val entryResult: EntryResult,
    private val maxLine: (()-> Int)? = null
): PrettyPrint{

    enum class EntryResult(val value: String){
        Positive("OK"),
        Negative("Fail")
    }

    private  val minDotsCount get() =  maxLine?.let {
        it.invoke() -  stepName.count() + stepBadge.count()
    }?:  4

    private val stepNameFormatted: String get() = stepName + ".".repeat(minDotsCount)

    override val formattedString: String get() =
        "$stepBadge $stepNameFormatted ".concat {
            stepResult.colorizeIf(Colour.Green, negativeCaseColour = Colour.Red){ entryResult == EntryResult.Positive }
        }
    override fun toString(): String = "$stepBadge $stepNameFormatted $stepResult"
}
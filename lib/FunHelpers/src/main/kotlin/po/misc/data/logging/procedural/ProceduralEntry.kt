package po.misc.data.logging.procedural

import po.misc.data.PrettyPrint
import po.misc.data.printable.Printable
import po.misc.data.styles.Colour

data class ProceduralEntry(
    val stepName: String,
    val stepBadge: String,
    val maxLine: (()-> Int)? = null
): PrettyPrint, Printable{

    var stepResult: StepResult = StepResult.InProgress

    var complete: (()-> Unit)? = null

    internal val subEntries = mutableListOf<ProceduralEntry>()

    fun onComplete(callback:()-> Unit){
        complete = callback
    }
    fun provideResult(result: StepResult){
        stepResult = result
        complete?.invoke()
    }

    private  val minDotsCount get() =  maxLine?.let {
        (it.invoke() -  stepName.count() + stepBadge.count()).coerceAtLeast(4)
    }?:  4

    private val stepNameFormatted: String get() = stepName + ".".repeat(minDotsCount)

    private val subEntriesString: String get() = subEntries.joinToString(separator = "") { it.formattedString }

    override val formattedString: String get() =
        "$stepBadge $stepNameFormatted ".concat {
            stepResult.colorizeIf(Colour.Green, negativeCaseColour = Colour.Red){ stepResult == StepResult.OK }
                }.concat { subEntriesString }

    override fun toString(): String = "$stepBadge $stepNameFormatted $stepResult"

    companion object{
        private fun stepResultForListType(result: List<*>,  tolerances : List<StepTolerance>): StepResult {
            return if (result.isEmpty() && tolerances.any { it ==   StepTolerance.ALLOW_EMPTY_LIST } ) {
                StepResult.OK
            } else {
                StepResult.Fail
            }
        }
        private fun stepResultForBoolean(result: Boolean, tolerances : List<StepTolerance>): StepResult{
          return  if(!result && tolerances.any { it ==  StepTolerance.ALLOW_FALSE}  ){
              StepResult.OK
            }else{
              StepResult.Fail
            }
        }

        fun toEntryResult(result: Any?, tolerances : List<StepTolerance> = emptyList()): StepResult{
            return  when(result){
                is List<*> -> stepResultForListType(result, tolerances)
                is Boolean -> stepResultForBoolean(result, tolerances)
                else -> {
                    if(result == null){
                        if(tolerances.any { it ==  StepTolerance.ALLOW_NULL} ){
                            StepResult.OK
                        }else{
                            StepResult.Fail
                        }
                    }else{
                        StepResult.OK
                    }
                }
            }
        }
    }
}
package po.misc.data.logging.procedural


data class ProceduralEntry(
    val stepName: String,
    val stepBadge: String,
    val stepResult: String,
    private val maxLine: (()-> Int)? = null
){

    private  val minDotsCount get() =  maxLine?.let {
        it.invoke() -  stepName.count() + stepBadge.count()
    }?:  4

    private val stepNameFormatted: String get() = stepName + ".".repeat(minDotsCount)

    override fun toString(): String = "$stepBadge $stepNameFormatted $stepResult"
}
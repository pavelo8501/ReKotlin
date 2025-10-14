package po.misc.context.models

import po.misc.data.styles.Colour
import po.misc.debugging.DebugFrame


data class IdentityData(
    override val contextName: String,
    override val uuid: String = "",
    override val typeData: String="",
    override val numericId: Long,
    override val isIdUsedDefined: Boolean = true,
    override val hashCode: Int,
    var message:String = "",
    var colour: Colour = Colour.Green
): DebugFrame{
    override fun toString(): String  {
      return  buildString {
            appendLine(contextName)
            appendLine("UUID : ${uuid}")
            appendLine("Type Parameters: ${typeData}")
            appendLine("numericId : $numericId")
            appendLine("Id User Defined : $isIdUsedDefined")
            appendLine("Hash Code : ${hashCode()}")
        }
    }
}
package po.misc.debugging

import po.misc.context.CTX
import po.misc.debugging.stack_tracer.StackFrameMeta


interface DebugFrame{
    val contextName: String
    val uuid: String
    val typeData: String
    val numericId: Long
    val isIdUsedDefined : Boolean
    val  hashCode: Int
}

data class DebugFrameData(
    override val contextName: String,
    override val uuid: String = "",
    override val typeData: String = "",
    override val numericId: Long = 0,
    val inContext: Any? = null
): DebugFrame{
    val frameMeta: MutableList<StackFrameMeta> = mutableListOf<StackFrameMeta>()
    override var isIdUsedDefined: Boolean = false
    override val hashCode: Int = 0

    fun setTrace(frame: List<StackFrameMeta>):DebugFrameData{
        frameMeta.addAll(frame)
        return this
    }
    constructor(inContext: CTX):this(
        contextName = inContext.contextName,
        uuid =  inContext.identity.uuid.toString(),
        typeData =inContext.identity.typeData.toString(),
        numericId =inContext.identity.numericId,
    ){

    }
}


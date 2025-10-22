package po.misc.data.logging.procedural

import po.misc.context.tracable.TraceableContext


class ProceduralFlow<H: TraceableContext, LR: ProceduralRecord>(
    val host: H,
    val subject: String,
    val logRecord: LR
){

    private var maxLineReg: Int = 4
        set(value) {
            if(value > field){
                field = value
            }
        }

    private fun entryResult(result: Any?): ProceduralEntry.EntryResult{
        return if(result == null){
            ProceduralEntry.EntryResult.Negative
        }else{
            when(result){
                is Boolean ->{
                    if(result){
                        ProceduralEntry.EntryResult.Positive
                    }else{
                        ProceduralEntry.EntryResult.Negative
                    }
                }
                else ->   ProceduralEntry.EntryResult.Positive
            }
        }
    }

    fun <R: Any?> proceduralStep(stepName: String, block: H.()->R):R{
        var inBlockThrowable : Throwable? = null

        val blockResult =  try {
            block(host)
        }catch (th: Throwable){
            inBlockThrowable = th
            null
        }
        //Later add formating for throwable

        val tempSolutionForBadge = "[SYNC]"
        val entryResult = entryResult(blockResult)
        val record = ProceduralEntry(stepName, tempSolutionForBadge, entryResult.value, entryResult){
            maxLineReg
        }
        maxLineReg = record.stepBadge.count() + record.stepName.count()
        logRecord.registerRecord(record)
        return  inBlockThrowable?.let {
            throw  it
        }?:run {
            @Suppress("UNCHECKED_CAST")
            blockResult as R
        }
    }
}
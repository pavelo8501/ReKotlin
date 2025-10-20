package po.misc.data.logging.procedural

import po.misc.context.component.Component



class ProceduralFlow<H: Component, LR: ProceduralRecord>(
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

    private fun formatResult(result: Any?): String{
        return if(result == null){
            "Fail"
        }else{
            when(result){
                is Boolean ->{
                    if(result){
                        "OK"
                    }else{
                        "Fail"
                    }
                }
                else -> "OK"
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
        val formatedResult = formatResult(blockResult)
        val record = ProceduralEntry(stepName, tempSolutionForBadge, formatedResult){
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
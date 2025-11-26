package po.misc.data.logging.procedural

import po.misc.context.log_provider.LogProvider

interface ProceduralHandler {

    val logProvider: LogProvider

    fun logStep(name: String): ProceduralEntry? {
        return logProvider.getHandler(ProceduralFlow::class)?.let { handler ->
            if (handler is ProceduralFlow<*>) {
                handler.createStep(name)
            } else {
                null
            }
        } ?: run {
            null
        }
    }

    fun completeStep(name: String? = null): Boolean {
        val flow = logProvider.getHandler(ProceduralFlow::class)
        if(flow is ProceduralFlow<*>){
            val found =  flow.findStep(name)
            if(found != null){
                flow.finalizeStep(found, Unit)
                return true
            }else{
                return false
            }
        }
        return false
    }

    fun <R> completeStep(result:R,  name: String? = null): Boolean {
        val flow = logProvider.getHandler(ProceduralFlow::class)
        if(flow is ProceduralFlow<*>){
            val found =  flow.findStep(name)
            if(found != null){
                flow.finalizeStep(found, result)
                return true
            }else{
                return false
            }
        }
        return false
    }

}
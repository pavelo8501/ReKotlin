package po.exposify.classes.components

import po.exposify.classes.interfaces.DataModel

class CallbackEmitter {

    var onSequenceLaunch: (suspend (sequenceName : String, data: List<*>?)-> Unit)? = null
    suspend fun <DATA: DataModel> callOnSequenceLaunch(sequenceName: String, data : List<DATA>? = null){
        onSequenceLaunch?.invoke(sequenceName, data)
    }


}
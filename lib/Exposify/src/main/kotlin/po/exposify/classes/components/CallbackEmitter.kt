package po.exposify.classes.components

import po.exposify.classes.interfaces.DataModel

class CallbackEmitter {

    var onSequenceLaunch: ((sequenceName : String, data: List<*>?)-> Unit)? = null
    fun <DATA: DataModel> callOnSequenceLaunch(sequenceName: String, data : List<DATA>? = null){
        onSequenceLaunch?.invoke(sequenceName, data)
    }


}
package po.db.data_service.scope.service.models

abstract class BaseTag(name: String = "") : SequenceTag(name){
    val tagType:String = "BaseTag"
}

sealed class SequenceTag(val key: String) {
    data object UserSequence : SequenceTag("user_sequence")
    data object OrderSequence : SequenceTag("order_sequence")
    data object ProductSequence : SequenceTag("product_sequence")

    companion object {
        private val registry = mutableMapOf<String, SequenceTag>()

        init {
//            val map = SequenceTag::class.sealedSubclasses.forEach {
//                if(it.objectInstance!= null){
//                    registry.putIfAbsent(it.simpleName!!, it.objectInstance)
//                }else{
//                    println(it.simpleName)
//                }
//            }
        }

        fun custom(key : String):SequenceTag{
           val new = object : BaseTag(key){}
           registry[key] = new
           return new
        }

    }


}

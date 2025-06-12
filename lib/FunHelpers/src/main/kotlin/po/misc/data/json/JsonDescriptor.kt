package po.misc.data.json


open class JsonDescriptor<T: Any>() {

    var someMethodToCreateJason: String = ""

    val jsonString: String get() = someMethodToCreateJason

    val jsonDelegates: MutableList<JsonDelegateBase<T, *>> = mutableListOf()

    fun registerDelegate(delegate : JsonDelegateBase<T, *>){
        jsonDelegates.add(delegate)
    }


    fun  serialize(value: T): String{
        var joinStr = ""
        jsonDelegates.forEach {
            joinStr = buildString {
                val string = it.toJson(value)
                println(string)
            }
        }
        someMethodToCreateJason = joinStr
        return joinStr
    }


}
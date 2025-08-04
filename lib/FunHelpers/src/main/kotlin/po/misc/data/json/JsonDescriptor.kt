package po.misc.data.json

import po.misc.data.printable.PrintableBase

open class JsonDescriptor<T: PrintableBase<T>>() {

    private var currentValue: T? = null
    private var cachedJson: String = ""

    val jsonDelegates: MutableList<JsonDelegateBase<T, *>> = mutableListOf()
    var jsonHolder : JsonHolder? = null
    var receiverProviderFn: ((T) -> JsonObjectDelegate<*, *, *>)? = null
    fun subscribeForReceiver(provider: (T) -> JsonObjectDelegate<*, *, *>){
        receiverProviderFn = provider
    }
    private fun provideReceiver(value: T):JsonObjectDelegate<*, *, *>?{
        return  receiverProviderFn?.invoke(value)
    }

    fun registerDelegate(delegate: JsonDelegateBase<T, *>) {
        jsonDelegates.add(delegate)
    }

    fun checkChild(value: T): List<JObject> {
        val entries : MutableList<JObject> = mutableListOf()

        if (value.arbitraryRecords.isNotEmpty()) {
            value.arbitraryRecords.forEach {
                entries.add(it.jsonObject)
            }
        }
        return entries
    }

    fun serialize(value: T): String {
        jsonHolder = value.jsonHolder
        value.defaultsToJson()?.let {defaultHolder->
            jsonDelegates.forEach {
                val jRecord = it.toJson(value)
                defaultHolder.addRecord(jRecord)
            }
            provideReceiver(value)?.jsonObject?.let { jObject->
                defaultHolder.addJsonObject(jObject)
            }
            jsonHolder = defaultHolder
        }
        println(jsonHolder)
        return cachedJson
    }

}
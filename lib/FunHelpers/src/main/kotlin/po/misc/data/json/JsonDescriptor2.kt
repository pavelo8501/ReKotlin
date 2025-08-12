package po.misc.data.json

import po.misc.data.json.models.JsonObject
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.types.TypeData
import kotlin.reflect.full.createType


abstract class JsonDescriptorBase<T: PrintableBase<T>>(
    val companion: PrintableCompanion<T>,
    val jsonArray: JsonObject<T>
){
    init {
        companion.jsonDescriptor = this
    }
}

class JsonDescriptor2<T: PrintableBase<T>>(
    companion: PrintableCompanion<T>,
    val builder: JsonObject<T>.()-> Unit
) : JsonDescriptorBase<T>(companion, JsonObject<T>(TypeData(companion.printableClass, companion.printableClass.createType()))) {

    init {
        build()
    }

    fun build(){
        jsonArray.builder()
    }
}


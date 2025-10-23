package po.misc.data.json

import po.misc.data.json.models.JsonObject
import po.misc.data.printable.Printable
import po.misc.data.printable.companion.PrintableCompanion


abstract class JsonDescriptorBase<T: Printable>(
    val companion: PrintableCompanion<T>,
    val jsonArray: JsonObject<T, T>
){

}

class JsonDescriptor<T: Printable>(
    companion: PrintableCompanion<T>,
    val builder: JsonObject<T, T>.()-> Unit

) : JsonDescriptorBase<T>(companion, JsonObject<T, T>(companion.typeToken)) {

    fun build(){
        jsonArray.builder()
    }
}


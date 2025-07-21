package po.misc.data.printable.knowntypes

import po.misc.data.printable.PrintableTemplate
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableCompanion
import po.misc.context.CTX

class PropertyData(
    val producer: CTX,
    val propertyName: String,
    val value: String,
): PrintableBase<PropertyData>(Property){

    override val self: PropertyData = this

    companion object: PrintableCompanion<PropertyData>({PropertyData::class}){

        val Property = PrintableTemplate<PropertyData>(){
            "$propertyName = $value"
        }

    }

}
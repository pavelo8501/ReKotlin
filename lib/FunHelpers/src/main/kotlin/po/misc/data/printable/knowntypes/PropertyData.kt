package po.misc.data.printable.knowntypes


import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.context.CTX
import po.misc.data.printable.companion.nextLine

class PropertyData(
    val producer: CTX,
    val propertyName: String,
    val value: String,
): PrintableBase<PropertyData>(this){

    override val self: PropertyData = this

    companion object: PrintableCompanion<PropertyData>({PropertyData::class}){

        val Property = createTemplate {
            nextLine {
                "$propertyName = $value"
            }
        }
    }

}
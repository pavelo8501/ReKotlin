package po.exposify.common.events

import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.printable.companion.nextLine


data class ContextData(
    val message: String,
): PrintableBase<ContextData>(this) {

    override val self: ContextData = this

    companion object : PrintableCompanion<ContextData>({ ContextData::class }) {

        val Debug = createTemplate {
            nextLine {
                ""
            }
        }
    }
}


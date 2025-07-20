package po.exposify.common.event

import po.exposify.dto.DTOBase
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableTemplate
import po.misc.data.printable.PrintableCompanion
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.context.Identifiable
import po.misc.interfaces.asIdentifiable

data class DTOClassData(
    private val dtoClass: DTOBase<*, *, *>,
    val message: String,
    val status : String = dtoClass.status.name,
    val cachedDtoCount : Int = dtoClass.dtoMapSize,
): PrintableBase<DTOClassData>(Success){
    override val self: DTOClassData = this

    override val producer: Identifiable = asIdentifiable("DTOClassData", "dtoClass")

    init {
        addTemplate(Info, Success, Warning)
    }

    companion object: PrintableCompanion<DTOClassData>({DTOClassData::class}){

        fun identity(data:DTOClassData): String{
            return "DTO: ${data.dtoClass.identity.completeName}. Status:${data.status}"
        }

        val Info : PrintableTemplate<DTOClassData> = PrintableTemplate("Info"){
            "${identity(this)}: Message: ${message.colorize(Colour.BRIGHT_WHITE)}"
        }

        val Success : PrintableTemplate<DTOClassData> = PrintableTemplate("Success"){
            "${identity(this)}: Message: ${message.colorize(Colour.GREEN)}"
        }

        val Warning : PrintableTemplate<DTOClassData> = PrintableTemplate("Warning"){
            "${identity(this)}: Message: ${message.colorize(Colour.YELLOW)}"
        }

        val Debug: PrintableTemplate<DTOClassData> = PrintableTemplate("Debug"){
            message
        }

    }

}

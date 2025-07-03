package po.exposify.common.event

import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.misc.data.printable.PrintableBase
import po.misc.data.console.PrintableTemplate
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased
import po.misc.interfaces.asIdentifiable

data class DTOClassEvent(
    private val dtoClass: DTOBase<*, *, *>,
    val message: String,
    val status : String = dtoClass.status.name,
    val cachedDtoCount : Int = dtoClass.dtoMapSize,
): PrintableBase<DTOClassEvent>(Success){
    override val self: DTOClassEvent = this

   // override val itemId: ValueBased = DTOClass
    override val emitter: Identifiable = asIdentifiable(dtoClass.identity.sourceName, dtoClass.contextName)

    init {
        addTemplate(Info, Success, Warning)
    }

    companion object{

        fun identity(data:DTOClassEvent): String{
            return "DTO: ${data.dtoClass.identity.completeName}. Status:${data.status}"
        }

        val Info : PrintableTemplate<DTOClassEvent> = PrintableTemplate("Info"){
            "${identity(this)}: Message: ${message.colorize(Colour.BRIGHT_WHITE)}"
        }

        val Success : PrintableTemplate<DTOClassEvent> = PrintableTemplate("Success"){
            "${identity(this)}: Message: ${message.colorize(Colour.GREEN)}"
        }

        val Warning : PrintableTemplate<DTOClassEvent> = PrintableTemplate("Warning"){
            "${identity(this)}: Message: ${message.colorize(Colour.YELLOW)}"
        }

    }

}

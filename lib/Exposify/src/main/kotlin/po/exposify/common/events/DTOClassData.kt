package po.exposify.common.event

import po.exposify.dto.DTOBase
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.data.printable.companion.Template

data class DTOClassData(
    private val dtoClass: DTOBase<*, *, *>,
    val message: String,
    val status : String = dtoClass.status.name,
    val cachedDtoCount : Int = dtoClass.dtoMapSize,
): PrintableBase<DTOClassData>(Success){
    override val self: DTOClassData = this

    init {
        addTemplate(Info, Success, Warning)
    }

    companion object: PrintableCompanion<DTOClassData>({DTOClassData::class}){
        fun identity(data:DTOClassData): String{
            return "DTO: ${data.dtoClass.identity}. Status:${data.status}"
        }
        val Info: Template<DTOClassData> = createTemplate{
            next { "${identity(this)}: Message: ${message.colorize(Colour.BRIGHT_WHITE)}" }
        }
        val Success: Template<DTOClassData> = createTemplate{
            next { "${identity(this)}: Message: ${message.colorize(Colour.GREEN)}"}
        }
        val Warning: Template<DTOClassData> = createTemplate{
            next { "${identity(this)}: Message: ${message.colorize(Colour.YELLOW)}" }
        }
        val Debug: Template<DTOClassData> = createTemplate{
            next { message }
        }
    }

}

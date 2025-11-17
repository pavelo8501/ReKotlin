package po.exposify.common.event

import po.exposify.dto.DTOBase
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.data.printable.companion.nextLine
import po.misc.types.token.TypeToken

data class DTOClassData(
    private val dtoClass: DTOBase<*, *, *>,
    val message: String,
    val status : String = dtoClass.status.name,
    val cachedDtoCount : Int = dtoClass.dtoConfiguration.dtoMap.size,
): PrintableBase<DTOClassData>(this){
    override val self: DTOClassData = this


    companion object: PrintableCompanion<DTOClassData>(TypeToken.create()){

        fun identity(data:DTOClassData): String{
            return "DTO: ${data.dtoClass.identity}. Status:${data.status}"
        }
        val Info = createTemplate{
            nextLine { "${identity(this)}: Message: ${message.colorize(Colour.WhiteBright)}" }
        }
        val Success = createTemplate {
            nextLine { "${identity(this)}: Message: ${message.colorize(Colour.Green)}" }
        }
        val Warning = createTemplate {
            nextLine { "${identity(this)}: Message: ${message.colorize(Colour.Yellow)}" }
        }
        val Debug = createTemplate {
            nextLine{ message }
        }
    }

}

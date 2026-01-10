package po.misc.data.pretty_print.parts.options

import po.misc.data.Named
import po.misc.data.PrettyPrint
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.parts.grid.RenderableType
import po.misc.types.token.TypeToken
import java.time.Instant


class CompositionTrace(
    val renderableType:RenderableType,
    val typeString: String,
    val templateID: NamedTemplate,
    val hashCode: Int = 0
){
    val created: Instant = Instant.now()

    var recreatedFrom: CompositionTrace? = null
    var consumedBy: CompositionTrace? = null

    val compositionRecords = mutableListOf<CompositionTrace>()

    companion object{

        fun createFrom(token1: TypeToken<*>, renderableType:RenderableType, templateID: NamedTemplate? = null):CompositionTrace{
            val templateName =  renderableType.name
            val useID = if(templateID != null){
                templateID
            }else{
                val typeName =  token1.typeName
                PrettyHelper.generateGridID(renderableType, typeName)
            }
            return CompositionTrace(renderableType, templateName, useID)
        }

        fun createFrom(token1: TypeToken<*>, token2: TypeToken<*>?, renderableType:RenderableType, templateID: NamedTemplate? = null):CompositionTrace{
            val templateName =  renderableType.name
            val typeName =  if(token2 != null){
                "${token1.typeName}, ${token2.typeName}"
            }else{
                token1.typeName
            }
           val useID = templateID ?: PrettyHelper.generateGridID(renderableType, typeName)
           return CompositionTrace(renderableType, templateName, useID)
        }
    }
}

class TemplateData(
    val templateID:NamedTemplate,
    internal  val compositionTrace: CompositionTrace,
): PrettyPrint{

    val receiverTypeName: String get() {
        return  compositionTrace.typeString
    }
    private val typeName:String get() = compositionTrace.typeString
    override val formattedString: String get() {
      return  when(templateID){
            is RowID, is GridID -> "${templateID.formattedString} <$receiverTypeName>"
        }
    }
    override fun toString(): String = typeName
}

sealed interface NamedTemplate: PrettyPrint, Named{
    val renderableType: RenderableType
    val typeName: String
    override val formattedString: String
}

interface RowID: NamedTemplate, RowBuildOption {
    override val renderableType: RenderableType get() = RenderableType.Row
    override val typeName: String  get() {
        return "Row #${name}"
    }
    override val formattedString: String
        get()  {
           return "${PrettyRow.prettyName} #$name"
        }
}

interface GridID: NamedTemplate {
    override val renderableType: RenderableType get() = RenderableType.Grid
    override val typeName: String  get() {
        return "Grid #${name}"
    }
    override val formattedString: String
        get() {
           return "${PrettyGrid.prettyName} #$name"
        }
}
class DefaultGridID internal constructor(
    override val renderableType: RenderableType,
    val receiverTypeName: String,
    val hash: Int
): GridID {
    override val name: String get() {
        return "${renderableType.name} #$hash <${receiverTypeName}>"
    }
    override val formattedString: String get() {
        return "${renderableType.formattedString} #$hash<${receiverTypeName}>"
    }
    override fun toString(): String {
        return name
    }
}

class DefaultRowID internal constructor(
    val receiverTypeName: String,
    val hash: Int
): RowID {

    override val name: String get() {
        return "${renderableType.name} #$hash <${receiverTypeName}>"
    }
    override val formattedString: String get() {
        return "${renderableType.formattedString} #$hash<${receiverTypeName}>"
    }
    override fun toString(): String {
        return name
    }
}



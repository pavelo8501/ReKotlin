package po.misc.data.pretty_print.parts.options

import po.misc.data.PrettyPrint
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.parts.grid.RenderableType
import po.misc.interfaces.named.Named
import po.misc.types.token.TokenHolder
import po.misc.types.token.TypeToken
import java.time.Instant

class CompositionTrace(
    val renderableType:RenderableType,
    val typeString: String,
    val templateID: NamedTemplate
){

    val hashCode: Int get() {
       return when(templateID){
            is DefaultGridID ->    templateID.hash
            is DefaultRowID ->    templateID.hash
            else ->   0
        }
    }
    val created: Instant = Instant.now()

    companion object{


        fun createFrom(
            tokenized: TokenHolder,
            renderableType:RenderableType,
            templateID: NamedTemplate? = null
        ):CompositionTrace {
            val templateName = renderableType.name
            val typeName = tokenized.typeName
            return templateID?.let {
                CompositionTrace(renderableType, templateName, it)
            } ?: run {
                CompositionTrace(renderableType, templateName, PrettyHelper.generateGridID(renderableType, typeName))
            }
        }

    }
}

class TemplateData(
    id:NamedTemplate
): PrettyPrint{

    constructor(compositionTrace: CompositionTrace):this(compositionTrace.templateID){
        compositionRecords.add(compositionTrace)
    }

    var templateID:NamedTemplate = id
        private set

    internal val compositionRecords = mutableListOf<CompositionTrace>()

    val receiverTypeName: String get() {
        return templateID.name
    }

    override val formattedString: String get() {
      return  when(templateID){
            is RowID, is GridID -> "${templateID.formattedString} <$receiverTypeName>"
        }
    }

    fun updateID(trace: CompositionTrace){
        templateID = trace.templateID
        compositionRecords.add(trace)
    }

    override fun toString(): String = templateID.name
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
        return "${renderableType.name}<${receiverTypeName}> #$hash"
    }
    override val formattedString: String get() {
        return "${renderableType.formattedString}<${receiverTypeName}> #$hash"
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



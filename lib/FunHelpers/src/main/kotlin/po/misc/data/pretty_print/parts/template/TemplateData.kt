package po.misc.data.pretty_print.parts.template

import po.misc.data.Named
import po.misc.data.PrettyPrint
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.parts.grid.RenderableType

class TemplateData(
    val templateID: NamedTemplate,
    private val receiverName: String,
): PrettyPrint{

    constructor(templateID: DefaultID):this(templateID, templateID.receiverTypeName){
        isGeneric = true
    }

    val receiverTypeName: String get() {
       return if(templateID is DefaultID){
            templateID.receiverTypeName
        }else{
            receiverName
        }
    }
    val renderableType: RenderableType = templateID.type
    private val typeName:String get() = templateID.typeName
    var isGeneric:Boolean = false
        private set

    override val formattedString: String get() {
      return  when(templateID){
            is RowID, is GridID -> "${templateID.formattedString} <$receiverTypeName>"
            else -> templateID.formattedString
        }
    }
    override fun toString(): String = typeName
}


sealed interface NamedTemplate: PrettyPrint, Named{
    val type: RenderableType
    val typeName: String
    override val formattedString: String
}

interface RowID : NamedTemplate{
    override val name: String

    override val type: RenderableType get() = RenderableType.Row
    override val typeName: String  get() {
        return "Row #${name}"
    }

    override val formattedString: String
        get()  {
           return "${PrettyRow.prettyName} #$name"
        }
}

interface GridID: NamedTemplate {

    override val name: String
    override val type: RenderableType get() = RenderableType.Grid
    override val typeName: String  get() {
        return "Grid #${name}"
    }
    override val formattedString: String
        get() {
           return "${PrettyGrid.prettyName} #$name"
        }
}


class DefaultID internal constructor(
    val receiverTypeName: String,
    override val type: RenderableType,
    val hash: Int
): NamedTemplate, Named{

    override val name: String get() = type.name

    override val typeName: String get() {
       return "$name #$hash <${receiverTypeName}>"
    }
    override val formattedString: String get() {
       return "${type.formattedString} #$hash<${receiverTypeName}>"
    }
    override fun toString(): String {
       return typeName
    }
}





package po.misc.debugging.stack_tracer

import po.misc.data.PrettyFormatted
import po.misc.data.PrettyPrint
import po.misc.data.pretty_print.grid.PrettyGrid
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.RenderOptions
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.debugging.classifier.PackageClassifier
import po.misc.types.k_class.simpleOrAnon

data class StackFrameMeta(
    val fileName: String,
    val simpleClassName: String,
    val methodName: String,
    val lineNumber: Int,
    val classPackage: String,
    val packageRole: PackageClassifier.PackageRole,
    val isReflection: Boolean,
    val isThreadEntry: Boolean,
    val isCoroutineInternal: Boolean,
    val isInline: Boolean,
    val isLambda: Boolean,
    val stackTraceElement: StackTraceElement? = null
): PrettyPrint, PrettyFormatted {

    enum class Template { Short,  ConsoleLink }

    val isHelperMethod: Boolean get() = packageRole == PackageClassifier.PackageRole.Helper
    val isUserCode: Boolean get() = packageRole != PackageClassifier.PackageRole.System

    val consoleLink: String get() = "$classPackage.$simpleClassName.$methodName($fileName:$lineNumber)"

    override val formattedString: String get() = frameMetaTemplate.render(this, RenderOptions(Template.Short))

    override fun formatted(sections: Collection<Enum<*>>?): String {
        return if(sections != null){
           val list = buildList {
                add(Template.Short)
                addAll(sections)
            }
            frameMetaTemplate.render(this, RenderOptions(list))
        }else{
            formattedString
        }
    }

    override fun toString(): String {
        val name = this::class.simpleOrAnon
        return "$name [Method name: $methodName,  Class name: $simpleClassName, Method name: $methodName,  Is helper: $isHelperMethod]"
    }

    companion object {
        val frameMetaTemplate: PrettyGrid<StackFrameMeta> = buildPrettyGrid {
            buildRow(RowOptions(Orientation.Vertical,  Template.Short)){
                addCells(StackFrameMeta::methodName, StackFrameMeta::lineNumber, StackFrameMeta::simpleClassName)
            }
            buildRow(RowOptions(Orientation.Vertical,  Template.ConsoleLink)) {
                addCell(StackFrameMeta::consoleLink)
            }
        }
    }
}
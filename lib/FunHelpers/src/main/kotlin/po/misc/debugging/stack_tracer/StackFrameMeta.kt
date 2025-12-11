package po.misc.debugging.stack_tracer

import po.misc.data.PrettyFormatted
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.misc.data.pretty_print.grid.buildRow
import po.misc.data.pretty_print.parts.KeyedOptions
import po.misc.data.pretty_print.parts.KeyedPresets
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.strings.appendGroup
import po.misc.debugging.classifier.PackageClassifier

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
): PrettyFormatted {

    enum class Template { ConsoleLink }

    val isHelperMethod: Boolean get() = packageRole == PackageClassifier.PackageRole.Helper
    val isUserCode: Boolean get() = packageRole != PackageClassifier.PackageRole.System
    val consoleLink: String get() = "$classPackage.$simpleClassName.$methodName($fileName:$lineNumber)"

    val formattedString: String get() {
       return frameTemplate.render(this){
           exclude(Template.ConsoleLink)
       }
    }
    override fun formatted(renderOnly: List<Enum<*>>?): String {
        return frameTemplate.render(this){
            renderOnly(renderOnly)
        }
    }
    override fun toString(): String {
        return buildString {
            appendGroup("StackFrameMeta[", "]", ::methodName, ::simpleClassName, ::methodName, ::isHelperMethod)
        }
    }

    companion object {

        private  val linkOptions = KeyedOptions(KeyedPresets.Property).usePlainValue(true)
        val frameTemplate: PrettyGrid<StackFrameMeta> = buildPrettyGrid<StackFrameMeta>(Orientation.Vertical) {
            buildRow(Orientation.Vertical){
                addCells(StackFrameMeta::methodName, StackFrameMeta::lineNumber, StackFrameMeta::simpleClassName)
            }
            buildRow(Template.ConsoleLink, Orientation.Vertical){
                addCell(StackFrameMeta::consoleLink, linkOptions)
            }
        }
    }
}
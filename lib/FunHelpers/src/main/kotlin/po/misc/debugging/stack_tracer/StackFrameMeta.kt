package po.misc.debugging.stack_tracer

import po.misc.data.PrettyFormatted
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.buildPrettyGrid
import po.misc.data.pretty_print.parts.options.CellPresets
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.RowID
import po.misc.data.pretty_print.buildPrettyRow
import po.misc.data.strings.appendGroup
import po.misc.debugging.classifier.PackageClassifier

data class StackFrameMeta(
    val fileName: String,
    val simpleClassName: String,
    val displayMethodName: String,
    val lineNumber: Int,
    val classPackage: String,
    val packageRole: PackageClassifier.PackageRole,
    val isReflection: Boolean,
    val isThreadEntry: Boolean,
    val isCoroutineInternal: Boolean,
    val isInline: Boolean,
    val isLambda: Boolean,
    val methodName:String,
    var index: Int = 0,
    val stackTraceElement: StackTraceElement? = null
): PrettyFormatted {

    data class KeyFrameParameter(
        val packageRole:PackageClassifier.PackageRole,
        val methodName: String?,
        val simpleClassName: String,
    )

    enum class Template: RowID { ConsoleLink }


    val isHelperMethod: Boolean get() = packageRole == PackageClassifier.PackageRole.Helper
    val isUserCode: Boolean get() = packageRole != PackageClassifier.PackageRole.System
    val consoleLink: String get() = "$classPackage.$simpleClassName.$methodName($fileName:$lineNumber)"

    val formattedString: String get() {
       return frameTemplate.render(this)
    }

    fun setIndex(index: Int):StackFrameMeta{
        this.index = index
        return this
    }

    override fun formatted(renderOnly: List<RowID>?): String {
        return frameTemplate.render(this)
    }
    override fun toString(): String {
        return buildString {
            appendGroup("StackFrameMeta[", "]", ::methodName, ::simpleClassName, ::methodName, ::isHelperMethod)
        }
    }

    companion object {
        private  val linkOptions = Options(CellPresets.Property).usePlainValue(true)
        val linkTemplate =  buildPrettyRow(Template.ConsoleLink){
            orientation = Orientation.Vertical
            add(StackFrameMeta::consoleLink, linkOptions)
        }
        val frameTemplate: PrettyGrid<StackFrameMeta> = buildPrettyGrid<StackFrameMeta> {
            orientation = Orientation.Vertical
            buildRow{
                orientation = Orientation.Vertical
                addAll(StackFrameMeta::methodName, StackFrameMeta::lineNumber, StackFrameMeta::simpleClassName)
            }
            buildRow{
                Orientation.Vertical
                add(StackFrameMeta::consoleLink, linkOptions)
            }
        }
    }
}
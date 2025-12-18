package po.misc.debugging.models

import po.misc.data.PrettyPrint
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.parts.RowPresets
import po.misc.data.pretty_print.rows.buildPrettyRow
import po.misc.debugging.stack_tracer.ExceptionTrace
import po.misc.debugging.stack_tracer.StackFrameMeta
import po.misc.time.TimeHelper


class InstanceInfo(
    val instanceName: String,
    val hash: Int,
    val classInfo: ClassInfo,
): PrettyPrint{

    val className: String = classInfo.simpleName
    override val formattedString: String get(){
        return template.render(this)
    }
    override fun toString(): String = template.render(this, RowPresets.VerticalPlain)

    companion object{

        val template: PrettyRow<InstanceInfo> = buildPrettyRow(Orientation.Vertical){
            add(InstanceInfo::instanceName)
            add(InstanceInfo::className)
            add(InstanceInfo::hash)
        }
    }

}


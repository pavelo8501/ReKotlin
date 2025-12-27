package po.misc.debugging.models

import po.misc.data.PrettyPrint
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.RowPresets
import po.misc.data.pretty_print.rows.buildPrettyRow
import po.misc.data.strings.appendGroup


class InstanceInfo(
    val instanceName: String,
    val hash: Int,
    val classInfo: ClassInfo,
): PrettyPrint{

    val className: String = classInfo.simpleName
    override val formattedString: String get(){
        return template.render(this)
    }
    override fun toString(): String = buildString {
        if(instanceName == className){
            append(instanceName, "#${hash}")
        }else{
            append(instanceName, "#${hash}", className)
        }
    }


    companion object{
        val template: PrettyRow<InstanceInfo> = buildPrettyRow{
            add(InstanceInfo::instanceName)
            add(InstanceInfo::className)
            add(InstanceInfo::hash)
        }
    }

}


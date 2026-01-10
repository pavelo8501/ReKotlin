package po.misc.debugging.models

import po.misc.data.PrettyPrint
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.buildPrettyRow


class InstanceInfo(
    val instanceName: String,
    val hash: Int,
    val classInfo: ClassInfo,
): PrettyPrint{

    val className: String = classInfo.simpleName
    override val formattedString: String get() = buildString {
        append("Instance Name: $instanceName ")
        append("ClassName: $className ")
        append("Hash: #${hash}")
    }
    override fun toString(): String = buildString {
        if(instanceName == className){
            append(instanceName, "#${hash}")
        }else{
            append(instanceName, "#${hash}", className)
        }
    }
}


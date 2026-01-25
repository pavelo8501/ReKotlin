package po.misc.debugging.models

import po.misc.data.PrettyPrint
import po.misc.data.Styled
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.buildPrettyRow
import po.misc.data.text_span.StyledPair
import po.misc.data.text_span.TextSpan
import po.misc.types.k_class.simpleOrAnon
import kotlin.reflect.KClass


class InstanceMeta(
    val instanceName: TextSpan,
    val hash: Int = 0,
    val kClass: KClass<*>?,
): Styled{

    private val className:String = kClass?.simpleOrAnon?:"Null"
    val uniqueName: Boolean get() = className != instanceName.plain

    val hashText:StyledPair = StyledPair(hash.toString())
    override val textSpan: TextSpan get() = instanceName

}


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


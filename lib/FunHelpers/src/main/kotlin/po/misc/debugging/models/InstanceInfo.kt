package po.misc.debugging.models

import po.misc.data.ComparableParameter
import po.misc.data.Postfix
import po.misc.data.PrettyPrint
import po.misc.data.Styled
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.buildPrettyRow
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.TextStyler
import po.misc.data.text_span.StyledPair
import po.misc.data.text_span.TextSpan
import po.misc.data.text_span.buildTextSpan
import po.misc.types.k_class.simpleOrAnon
import kotlin.reflect.KClass


class InstanceMeta(
    name: String,
    hash: Int,
    val classInfo: ClassMeta,
): Styled, TextStyler{

    val instanceName: ComparableParameter = ComparableParameter("Instance Name", name)
    val classText: ComparableParameter = ComparableParameter("Class Name", classInfo.simpleName)
    val hashText: ComparableParameter = ComparableParameter("Hash", hash)

    override val textSpan: StyledPair get() = buildSpan {
        append(instanceName.text, instanceName.styledText, Postfix(SpecialChars.COMA))
        append(classText.text, classText.styledText, Postfix(SpecialChars.COMA))
        append(hashText.text, hashText.styledText)
    }

    fun compareParameters(other: InstanceMeta){
        instanceName.compareBoth(other.instanceName)
        classText.compareBoth(other.classText)
        hashText.compareBoth(other.hashText)
    }

    override fun toString(): String = textSpan.plain

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


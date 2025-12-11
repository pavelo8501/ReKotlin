package po.misc.debugging.classifier

import po.misc.data.strings.appendGroup

/**
 * Describes a helper class and optionally specific method names inside it.
 *
 * A [HelperRecord] allows the classifier to treat certain classes or
 * individual methods as internal infrastructure, and therefore as helpers.
 *
 * @property helperClassName the simple class name that should be recognized as a helper
 * @property helperMethodNames a list of method names belonging to this helper class
 */
class HelperRecord(
    val helperClassName: String,
    val helperMethodNames : List<String>
){

    constructor(helperClassName: String, vararg helperMethodName : String):this(helperClassName, helperMethodName.toList())


  //  val helperMethodNames: List<String> = helperMethodName.toList()

    /**
     * Returns `true` if the given [methodName] matches one of this helper’s method names.
     */
    fun methodNameListed(methodName: String): Boolean{
        return methodName in helperMethodNames
    }

    fun copy(): HelperRecord {
       return  HelperRecord(helperClassName, helperMethodNames.toList())
    }

    override fun toString(): String {
        val names = helperMethodNames.joinToString()
        return buildString {
            append("HelperRecord $helperClassName")
            append("[{$names}]")
        }
    }
}
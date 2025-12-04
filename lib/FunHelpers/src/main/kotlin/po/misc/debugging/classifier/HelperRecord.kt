package po.misc.debugging.classifier

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
    vararg helperMethodName : String
){
    /** List of helper method names associated with this class. */
    val helperMethodNames: List<String> = helperMethodName.toList()

    /**
     * Returns `true` if the given [methodName] matches one of this helper’s method names.
     */
    fun methodNameListed(methodName: String): Boolean{
        return methodName in helperMethodNames
    }
}
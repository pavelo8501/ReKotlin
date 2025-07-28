package po.misc.exceptions.models

data class StackFrameMeta(
    val className: String,
    val methodName: String,
    val lineNumber: Int,
    val classPackage: String,
    val isHelperMethod: Boolean,
    val isUserCode: Boolean
)
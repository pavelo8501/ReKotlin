package po.misc.exceptions.models

data class StackFrameMeta(
    val fileName: String,
    val simpleClassName: String,
    val methodName: String,
    val lineNumber: Int,
    val classPackage: String,
    val isHelperMethod: Boolean,
    val isUserCode: Boolean
){


    override fun toString(): String {
       return buildString {
            appendLine("File name: $fileName")
            appendLine("Simple class name: $simpleClassName")
            appendLine("Method name: $methodName")
            appendLine("Line number: $lineNumber")
            appendLine("Class package: $classPackage")
            appendLine("Is helper method: $isHelperMethod")
            appendLine("Is user code: $isUserCode")
        }
    }

}


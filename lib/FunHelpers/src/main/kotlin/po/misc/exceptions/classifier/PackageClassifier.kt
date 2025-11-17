package po.misc.exceptions.classifier

import po.misc.exceptions.stack_trace.isHelperMethod

enum class PackageRole {
    Helper,
    User,
    Unknown
}

fun classifyPackage(
    traceElement: StackTraceElement,
    definitelyNotUserPrefixes : List<String> = listOf("kotlin", "java", "sun", "jdk", "org.jetbrains"),
    helperPrefixes: List<String> = listOf("po.misc", "kotlin", "java"),
    userPrefixes: List<String> = listOf("po.test", "com.myapp")
): PackageRole {

    val classPackage = traceElement.javaClass.packageName
    val notUser =
        definitelyNotUserPrefixes.any { prefix -> classPackage == prefix || classPackage.startsWith("$prefix.") }
    if (notUser) {
        return Unknown
    }
    val isHelper = helperPrefixes.any { prefix -> classPackage == prefix || classPackage.startsWith("$prefix.") }
    if (!isHelper) {
        if (traceElement.isHelperMethod()) {
            return PackageRole.Helper
        }
    }
    val isUser = userPrefixes.any { prefix -> classPackage == prefix || classPackage.startsWith("$prefix.") }
    if (isUser) {
        return PackageRole.User
    }
    return PackageRole.Unknown
}

fun classifyPackage(
    classPackage: String,
    definitelyNotUserPrefixes : List<String> = listOf("kotlin", "java", "sun", "jdk", "org.jetbrains"),
    helperPrefixes: List<String> = listOf("po.misc", "kotlin", "java"),
    userPrefixes: List<String> = listOf("po.test", "com.myapp")
): PackageRole {

    val notUser =  definitelyNotUserPrefixes.any { prefix -> classPackage == prefix || classPackage.startsWith("$prefix.")  }
    if(notUser){
        return Unknown
    }
    return when {
        helperPrefixes.any { prefix -> classPackage == prefix || classPackage.startsWith("$prefix.") } ->
            PackageRole.Helper

        userPrefixes.any { prefix -> classPackage == prefix || classPackage.startsWith("$prefix.") } ->
            PackageRole.User

        else -> PackageRole.Unknown
    }
}



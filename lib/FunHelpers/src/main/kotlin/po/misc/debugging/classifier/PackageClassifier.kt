package po.misc.debugging.classifier

import po.misc.debugging.classifier.PackageClassifier.PackageRole
import po.misc.debugging.normalizedMethodName

/**
 * Classifies stack trace elements into semantic package roles such as:
 * - [PackageRole.Helper] – internal utility/helper infrastructure
 * - [PackageRole.User] – user-facing or application code
 * - [PackageRole.System] – JVM/Kotlin standard library or platform code
 * - [PackageRole.Unknown] – could not be reliably determined
 *
 * A classifier implementation is used by tracing or logging systems to
 * distinguish “noise” stack frames from meaningful user frames.
 */
interface PackageClassifier{

    /**
     * Defines how a stack trace element should be classified.
     */
    enum class PackageRole { Helper, User, System, Unknown }

    /**
     * If `true`, classes ending with the Kotlin-generated `Kt` postfix
     * (e.g. `FileNameKt`) will always be treated as [PackageRole.Helper].
     *
     * This is useful when top-level functions represent internal utilities.
     */
    var ktPostfixAsHelper: Boolean

    /**
     * Determines the role of a given [StackTraceElement].
     *
     * @param element the stack trace element to classify
     * @return the identified [PackageRole] based on package, class, or method heuristics
     */
    fun resolvePackageRole(element: StackTraceElement):PackageRole
}

/**
 * A simple implementation of [PackageClassifier] that classifies stack trace
 * elements using:
 *
 * 1. System package prefixes (e.g. `kotlin.*`, `java.*`, `sun.*`)
 * 2. Explicit helper class/method annotations via [HelperRecord]
 * 3. The optional `Kt` postfix rule for Kotlin-generated top-level classes
 *
 * This classifier is suitable for tracing/logging frameworks that need
 * coarse-grained separation between:
 * - user-facing stack frames,
 * - helper/infrastructure frames,
 * - system-level frames.
 *
 * @constructor provides an optional initial list of helper class records
 */
open class SimplePackageClassifier(
    vararg helperClass: HelperRecord
):PackageClassifier {

    /**
     * Secondary constructor that accepts a [HelperClassList].
     */
    constructor(helpersList:  HelperClassList):this(){
        helperClassRecords = helpersList.classRecords.toMutableList()
    }

    private var helperClassRecords : MutableList<HelperRecord> = helperClass.toMutableList()

    override var ktPostfixAsHelper: Boolean = true

    /**
     * A list of well-known package prefixes that always map to
     * [PackageClassifier.PackageRole.System].
     */
    val definitelyNotUserPrefixes: List<String> = listOf("kotlin", "java", "sun", "jdk", "org.jetbrains")

    private fun filterOutNotUser(element: StackTraceElement): PackageRole{
        val classPackage : String = element.className.substringBeforeLast(".")
        val notUser =  definitelyNotUserPrefixes.any { prefix -> classPackage == prefix || classPackage.startsWith("$prefix.")  }
        return if(notUser){
            PackageRole.System
        }else{
            PackageRole.User
        }
    }

    private fun checkByClassName(className: String, roleByPreviousCheck: PackageRole = PackageRole.Unknown): PackageRole {
        if(ktPostfixAsHelper){
           val hasKT = className.contains("Kt")
            if(hasKT){
                return PackageRole.Helper
            }
        }
        val helperClassNames = helperClassRecords.filter { it.helperMethodNames.isEmpty() }.map { it.helperClassName }
        if(className in helperClassNames){
            return PackageRole.Helper
        }
        return roleByPreviousCheck
    }

    private fun checkByClassName(element: StackTraceElement, roleByPreviousCheck: PackageRole = PackageRole.Unknown): PackageRole {
        val className = element.className.substringAfterLast('.')
        return checkByClassName(className, roleByPreviousCheck)
    }

    private fun checkByMethodName(element: StackTraceElement, roleByPreviousCheck: PackageRole = PackageRole.Unknown): PackageRole{
        val methodName = element.normalizedMethodName()
        for(helperRecord in helperClassRecords){
            if(helperRecord.methodNameListed(methodName)){
                return PackageRole.Helper
            }
        }
        return roleByPreviousCheck
    }

    fun addHelperRecord(record: HelperRecord):SimplePackageClassifier{
        helperClassRecords.add(record)
        return this
    }

    fun addHelperRecords(records: List<HelperRecord>):SimplePackageClassifier{
        records.forEach { addHelperRecord(it) }
        return this
    }

    override fun resolvePackageRole(element: StackTraceElement):PackageRole{
        val role =  filterOutNotUser(element)
        if(role == PackageRole.System){
            return role
        }
        val roleByClass = checkByClassName(element, role)
        if(roleByClass == PackageRole.Unknown || roleByClass  ==  PackageRole.User){
            return checkByMethodName(element, roleByClass)
        }
        return roleByClass
    }
}

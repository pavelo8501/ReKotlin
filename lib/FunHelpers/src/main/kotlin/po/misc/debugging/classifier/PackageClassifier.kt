package po.misc.debugging.classifier

import po.misc.debugging.classifier.PackageClassifier.PackageRole
import po.misc.debugging.normalizedMethodName


interface PackageClassifier{
    enum class PackageRole { Helper, User, System, Unknown }

    var ktPostfixAsHelper: Boolean

    fun resolvePackageRole(element: StackTraceElement):PackageRole
}

class HelperRecord(
    val helperClassName: String,
    vararg helperMethodName : String
){
    val helperMethodNames: List<String> = helperMethodName.toList()

    fun methodNameListed(methodName: String): Boolean{
        return methodName in helperMethodNames
    }
}

open class SimplePackageClassifier(
    vararg helperClass: HelperRecord
):PackageClassifier {

    constructor(helpersList:  HelperClassList):this(){
        helperClassRecords = helpersList.classRecords.toMutableList()
    }

    private var helperClassRecords : MutableList<HelperRecord> = helperClass.toMutableList()

    override var ktPostfixAsHelper: Boolean = true
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

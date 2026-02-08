package po.test.misc.debugging.classifier

import po.misc.collections.asList
import po.misc.debugging.classifier.HelperClassList
import po.misc.debugging.classifier.HelperRecord
import po.misc.exceptions.Tracer
import po.misc.debugging.classifier.KnownHelpers
import po.misc.debugging.classifier.PackageClassifier
import po.misc.debugging.classifier.SimplePackageClassifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestPackageClassifier {

    private class HelperList(
        override val classRecords: List<HelperRecord>
    ): HelperClassList

    private fun firstTraceElement(): StackTraceElement{
        return Tracer().firstElement
    }
    private fun firstElementAsHelper(): StackTraceElement{
        return Tracer().firstElement
    }
    private fun elementByMethodNotMentioned(): StackTraceElement{
        return Tracer().firstElement
    }
    private val thisClassRecord = HelperRecord("TestPackageClassifier", "firstTraceElement")
    private val additionalRecord = HelperRecord("AdditionalRecord")

    @Test
    fun `Initialization by KnownHelpers and additional record`(){
        val classifier = SimplePackageClassifier(KnownHelpers, HelperRecord("TestPackageClassifier", "firstTraceElement"))
        assertEquals(6, classifier.records.size)
    }

    @Test
    fun `Empty constructor will use  KnownHelpers by default`(){
        val classifier = SimplePackageClassifier()
        assertEquals(5, classifier.records.size)
    }

    @Test
    fun `Using HelperRecord as source`(){
        val record = HelperRecord("TestPackageClassifier", "firstTraceElement")
        val classifier = SimplePackageClassifier(record.asList())
        assertEquals(1, classifier.records.size, "Passed in custom list overrides KnownHelpers default")
        classifier.addHelperRecord(additionalRecord)
        assertEquals(2, classifier.records.size)
        classifier.addHelperRecord(record.copy())
        assertEquals(2, classifier.records.size)
    }

    @Test
    fun `Using HelperClassList implementor as source`(){
        val helperList = HelperList(listOf(thisClassRecord, additionalRecord))
        val newRecord = HelperRecord("NewRecord")
        val classifier = SimplePackageClassifier(helperList)
        assertEquals(2, classifier.records.size, "Passed in object implementing  HelperClassList overwrites KnownHelpers")
        classifier.addHelperRecord(newRecord)
        assertEquals(3, classifier.records.size)
    }

    @Test
    fun `Classifier correctly resolves helper methods by explicit input`(){
        val classifier = SimplePackageClassifier(HelperRecord("TestPackageClassifier", "firstTraceElement").asList())
        val firstFrame = firstTraceElement()
        assertTrue { firstFrame.methodName.contains("firstTraceElement") }
        assertEquals(PackageClassifier.PackageRole.Helper, classifier.resolvePackageRole(firstFrame))
    }

    @Test
    fun `Classifier correctly resolves helper methods by adding HelperClassRecord`(){
        val record = HelperRecord("TestPackageClassifier", "firstTraceElement")
        val classifier = SimplePackageClassifier(record.asList())
        val firstFrame = firstTraceElement()
        val role =  classifier.resolvePackageRole(firstFrame)
        assertTrue { firstFrame.methodName.contains("firstTraceElement") }
        assertEquals(PackageClassifier.PackageRole.Helper, role)
    }

    @Test
    fun `Classifier should not resolve by class name if method names specified`(){
        val record = HelperRecord("TestPackageClassifier", "firstTraceElement")

        val classifier = SimplePackageClassifier(record.asList())
        val firstFrame = elementByMethodNotMentioned()
        val packageRole = classifier.resolvePackageRole(firstFrame)
        assertTrue { firstFrame.methodName.contains("elementByMethodNotMentioned") }
        assertFalse { packageRole ==  PackageClassifier.PackageRole.Helper }
    }

    @Test
    fun `Classifier correctly resolves helper methods by KnownHelpers object`(){
        val classifier = SimplePackageClassifier(KnownHelpers)
        val firstFrame = firstElementAsHelper()
        val packageRole  = classifier.resolvePackageRole(firstFrame)
        assertTrue { firstFrame.methodName.contains("firstElementAsHelper") }
        assertEquals(PackageClassifier.PackageRole.Helper, packageRole)
    }

}
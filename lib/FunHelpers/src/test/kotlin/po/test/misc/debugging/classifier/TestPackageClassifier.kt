package po.test.misc.debugging.classifier

import po.misc.debugging.classifier.HelperRecord
import po.misc.exceptions.Tracer
import po.misc.debugging.classifier.KnownHelpers
import po.misc.debugging.classifier.PackageClassifier
import po.misc.debugging.classifier.SimplePackageClassifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestPackageClassifier {

    private fun firstTraceElement(): StackTraceElement{
        return Tracer().firstTraceElement
    }

    private fun firstElementAsHelper(): StackTraceElement{
        return Tracer().firstTraceElement
    }

    private fun elementByMethodNotMentioned(): StackTraceElement{
        return Tracer().firstTraceElement
    }

    @Test
    fun `Classifier correctly resolves helper methods by explicit input`(){
        val classifier = SimplePackageClassifier(HelperRecord("TestPackageClassifier", "firstTraceElement"))
        val firstFrame = firstTraceElement()
        assertTrue { firstFrame.methodName.contains("firstTraceElement") }
        assertEquals(PackageClassifier.PackageRole.Helper, classifier.resolvePackageRole(firstFrame))
    }

    @Test
    fun `Classifier correctly resolves helper methods by adding HelperClassRecord`(){
        val classifier = SimplePackageClassifier()
        classifier.addHelperRecord(HelperRecord("TestPackageClassifier", "firstTraceElement"))
        val firstFrame = firstTraceElement()
        assertTrue { firstFrame.methodName.contains("firstTraceElement") }
        assertEquals(PackageClassifier.PackageRole.Helper, classifier.resolvePackageRole(firstFrame))
    }

    @Test
    fun `Classifier should not resolve by class name if method names specified`(){
        val classifier = SimplePackageClassifier(HelperRecord("TestPackageClassifier", "firstTraceElement"))
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
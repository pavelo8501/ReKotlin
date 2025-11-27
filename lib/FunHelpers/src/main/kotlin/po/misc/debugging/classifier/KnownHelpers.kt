package po.misc.debugging.classifier

interface HelperClassList {
    val classRecords: List<HelperRecord>
}

object KnownHelpers: HelperClassList{

    private val testPackageClassifier: HelperRecord = HelperRecord("TestPackageClassifier", "firstElementAsHelper")

    private val traceableContext: HelperRecord = HelperRecord(
        "TraceableContext",
        "notify",
        "notification"
    )
    private val component: HelperRecord = HelperRecord("Component")
    private val resolver  = HelperRecord("TraceResolver")
    private val stackTracer = HelperRecord("StackTracer")

    override val classRecords: List<HelperRecord> get() {
       return listOf(
           testPackageClassifier,
           traceableContext,
           component,
           stackTracer,
           resolver
       )
    }

}
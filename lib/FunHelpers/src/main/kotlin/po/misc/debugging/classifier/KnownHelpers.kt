package po.misc.debugging.classifier

import po.misc.callbacks.callable.FunctionCallableBase
import po.misc.callbacks.callable.PropertyCallable
import po.misc.callbacks.callable.ReceiverCallable

/**
 * Defines a contract for providing a collection of helper-class descriptors.
 *
 * Implementations expose a list of [HelperRecord] instances that describe
 * framework-, system-, or library-level classes whose stack-trace frames
 * should be classified as `PackageRole.Helper`.
 *
 * This interface allows grouping helper classes into reusable predefined sets.
 */
interface HelperClassList {
    val classRecords: List<HelperRecord>
}

/**
 * A predefined set of helper-class descriptors used across the project.
 *
 * This object centralizes the list of internal utility components that are
 * considered *helper-level* in stack-trace classification.
 *
 * The intention is to provide:
 * - a single place to register commonly used helper classes,
 * - predictable classification results,
 * - and avoid forgetting which internal classes should be treated as non-user code.
 *
 * Extend or modify this list when new helper components appear in the system.
 */
object KnownHelpers: HelperClassList {

    private val testPackageClassifier: HelperRecord = HelperRecord("TestPackageClassifier", "firstElementAsHelper")

    private val traceableContext = HelperRecord("TraceableContext")
    private val component = HelperRecord("Component")
    private val resolver = HelperRecord("TraceResolver")
    private val stackTracer = HelperRecord("StackTracer")
    private val callables = listOf(
        HelperRecord(FunctionCallableBase::class),
        HelperRecord(ReceiverCallable::class),
        HelperRecord(PropertyCallable::class)
    )

    override val classRecords: List<HelperRecord> = buildList {
        add(testPackageClassifier)
        add(traceableContext)
        add(component)
        add(stackTracer)
        add(resolver)
        addAll(callables)
    }

}
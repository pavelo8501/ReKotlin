package po.misc.functions.containers



class TaggedNotifier<E: Enum<E>, V: Any>(
    val tag: E,
    function: (V) -> Unit
): Notifier<V>(function) {
    override val identifiedAs: String get() = "Notifier<${tag.name}, V>"
}




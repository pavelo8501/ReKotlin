package po.misc.counters.parts

class AccessJournalDefaults<E: Enum<E>>(
    val defaultRecordType: E,
    val successRecordType: E,
    val failureRecordType: E,
)
package po.misc.counters.records

import po.misc.data.PrettyPrint

interface JournalEntry<E: Enum<E>>: PrettyPrint{

    val message: String
    val entryType: E
    fun resultOK(successType: E,  message: String? = null)
    fun resultFailure(failureType: E,  reason: String)
}
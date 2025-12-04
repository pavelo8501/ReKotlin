package po.misc.counters

import po.misc.context.tracable.TraceableContext
import po.misc.counters.records.AccessRecord
import po.misc.debugging.ClassResolver


fun <T: TraceableContext, E: Enum<E>>  T.createAccessJournal(defaultRecord: E): AccessJournal<E> {
   return  AccessJournal(ClassResolver.instanceInfo(this), defaultRecord)
}

fun <T: Any,  E: Enum<E>> T.createRecord(
    journal : AccessJournal<E>,
): AccessRecord<E> {
   return AccessRecord( ClassResolver.instanceInfo(this).instanceName, journal)
}


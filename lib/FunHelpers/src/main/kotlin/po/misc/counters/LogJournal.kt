package po.misc.counters

import po.misc.context.tracable.TraceableContext
import po.misc.counters.parts.AccessJournalDefaults
import po.misc.debugging.ClassResolver
import po.misc.debugging.models.InstanceInfo


class LogJournal<E: Enum<E>>(
    hostInstanceInfo: InstanceInfo,
    defaultRecordType: E,
    recordDefaults: AccessJournalDefaults<E>? = null
) : JournalBase<E>(hostInstanceInfo, defaultRecordType, recordDefaults){

    constructor(
        hostInstance: TraceableContext,
        defaultRecordType: E,
        recordDefaults: AccessJournalDefaults<E>? = null
    ):this(ClassResolver.instanceInfo(hostInstance), defaultRecordType, recordDefaults)





}
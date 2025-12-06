package po.test.misc.counters

import org.junit.jupiter.api.Test
import po.misc.context.component.Component
import po.misc.counters.AccessJournal
import po.misc.counters.records.AccessRecord
import po.misc.counters.createAccessJournal
import po.misc.counters.createRecord
import po.misc.data.output.output
import po.misc.data.pretty_print.Templated
import po.misc.data.pretty_print.parts.KeyedOptions
import po.misc.data.pretty_print.rows.buildPrettyRow
import po.misc.debugging.ClassResolver
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestAccessJournal: Component, Templated {

    private enum class TestAccess { Access, Read, Fail }

    interface InstanceInterface

    private class Instance1 : InstanceInterface
    private class Instance2 : InstanceInterface

    private fun create(instanceId: Int): InstanceInterface {
        return if (instanceId <= 1) {
            Instance1()
        } else {
            Instance2()
        }
    }

    @Test
    fun `Row initialization by property1`() {
        val accessJournal = createAccessJournal(TestAccess.Access)
        val instance1 = Instance1()
        val record: AccessRecord<TestAccess> = instance1.createRecord(accessJournal)
        val noKeyOption = KeyedOptions(showKey = false)
        val prettyRow = buildPrettyRow<AccessRecord<TestAccess>> {
            addCell(AccessRecord<TestAccess>::formatedTime, noKeyOption)
            addCell(AccessRecord<TestAccess>::message)
            addCell(AccessRecord<TestAccess>::hostName)
            addCell(AccessRecord<TestAccess>::entryType, noKeyOption)
        }
        assertEquals(4, prettyRow.cells.size)
        val result = prettyRow.renderAny(record)
        result.output()
        assertTrue { result.contains(record.message) }
    }

    @Test
    fun `AccessJournal stores records correctly`() {
        val journal = AccessJournal(ClassResolver.instanceInfo(this),  TestAccess.Access)
        for (i in 1..10) {
            val instance = if (i < 5) {
                create(1)
            } else {
                create(2)
            }
            journal.registerAccess(instance)
        }
        journal.output()
        assertEquals(10, journal.size)
    }

    @Test
    fun `AccessJournal records`() {
        val journal = AccessJournal(ClassResolver.instanceInfo(this),  TestAccess.Access)
        val record = journal.registerAccess(create(1))
        record.resultOK(TestAccess.Read)
        val record2 = journal.registerAccess(create(1))
        record2.resultFailure(TestAccess.Fail, "Some failure message")
        journal.print()
        journal.clear()
    }

}
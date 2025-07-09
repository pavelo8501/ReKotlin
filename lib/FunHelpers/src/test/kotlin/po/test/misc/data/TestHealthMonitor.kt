package po.test.misc.data

import org.junit.jupiter.api.Test
import po.misc.data.monitor.HealthMonitor
import po.misc.data.monitor.LifecyclePhase
import po.misc.data.monitor.MonitorAction
import po.misc.interfaces.CTX
import po.test.misc.setup.ControlClass
import kotlin.test.assertEquals

class TestHealthMonitor {


    class Monitored() : ControlClass(), CTX {
        override val contextName: String get() = "Monitored"
        val monitor: HealthMonitor<Monitored> = HealthMonitor(this)
        init {
            monitor.phase(LifecyclePhase.Initialization)
        }
    }

    @Test
    fun `Health monitor create and groups records in the correct way`() {
        val monitored: Monitored = Monitored()
        assertEquals(2, monitored.monitor.phases.size, "Not all phases created")
        assertEquals(LifecyclePhase.Construction, monitored.monitor.phases.first())
        assertEquals(LifecyclePhase.Initialization, monitored.monitor.phases.last())

        monitored.monitor.phase(LifecyclePhase.Main)
        monitored.monitor.input("parameter1","something")
        monitored.monitor.input("parameter2","something_else")

        val records = monitored.monitor.records()

        assertEquals(3, monitored.monitor.phases.size, "Not all phases created")
        assertEquals(3, records.size, "Records not saved")

        assertEquals(MonitorAction.Start, records.first().action)
        assertEquals("Monitored", records.first().parameter)

        assertEquals("parameter2", records.last().parameter)
        assertEquals("something_else", records.last().value)

        monitored.monitor.print(LifecyclePhase.Main)
        monitored.monitor.print()

    }

}
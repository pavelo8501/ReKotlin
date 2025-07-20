package po.misc.data.monitor

import po.misc.data.printable.PrintableTemplate
import po.misc.data.helpers.emptyOnNull
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableCompanion
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.colorize
import po.misc.context.CTX
import po.misc.reflection.anotations.ManagedProperty
import po.misc.reflection.properties.takePropertySnapshot
import po.misc.types.isNotNull
import java.time.LocalTime

enum class MonitorAction{
    Start,
    Stop,
    DataInput,
    Action,
    Result,
    TestedParameter
}


class HealthMonitor<T: CTX>(
    private val source:T
) {
    data class Record(
        val holder: CTX,
        val phase: LifecyclePhase,
        val action: MonitorAction,
        val parameter: String,
        var value: String = "N/A",
        var message: String? = null
    ): PrintableBase<Record>(Default){
        override val self: Record = this
        override val producer: CTX get() = holder
        val dateTime: LocalTime = LocalTime.now()

        companion object: PrintableCompanion<Record>({Record::class}){
            val Default = PrintableTemplate<Record>(){
                "${dateTime.toString()} : ${action.name} -> ($parameter = $value) ${message.emptyOnNull()} "
            }
        }
    }

    private val healthJournal: MutableMap<LifecyclePhase, MutableList<Record>> = mutableMapOf()
    internal val phases: List<LifecyclePhase> get() = healthJournal.keys.toList()
    internal val lastRecordOfActivePhase:Record? get() = phases.lastOrNull()?.let { healthJournal[it]?.lastOrNull() }
    internal val activePhase:LifecyclePhase get() = healthJournal.keys.lastOrNull()?:LifecyclePhase.Construction

    init {
        phase(LifecyclePhase.Construction)
    }
    private fun finalizePhase(lastRecord: Record){
        healthJournal[lastRecord.phase]?.add(Record(source, lastRecord.phase, MonitorAction.Stop, source.completeName))
    }

    private fun prebuildRecord(action: MonitorAction, parameter: String):Record{
       return   Record(source, activePhase, action, parameter)
    }

    private fun addRecord(record: Record){
        healthJournal[activePhase]?.add(record)
    }

    private fun prepareForCrash(record: Record){
        addRecord(record)
        print()
        val snapshot = takePropertySnapshot<T, ManagedProperty>(source)
        if(snapshot.isNotEmpty()){
            println("Before crash  property snapshot")
           val snapshotStr =  snapshot.joinToString(separator = SpecialChars.NewLine.char) { "val ${it.propertyName} = ${it.value}" }
            println(snapshotStr)
        }
    }

    fun phase(phase: LifecyclePhase){
        lastRecordOfActivePhase?.let { finalizePhase(it) }
        healthJournal.put(phase, mutableListOf(Record(source, phase, MonitorAction.Start, source.completeName)))
    }


    fun input(parameter: String, value: String, message: String? = null) {
        val action : MonitorAction = MonitorAction.DataInput
        val record = Record(source, activePhase, action, parameter, value, message.takeIf { it.isNotNull() } ?: "N/A")
        addRecord(record)
    }

    fun <T: Any?> action(parameter: String, value: String, evaluator:()->T):T {
        val record =  prebuildRecord(MonitorAction.Action, parameter).also {
            it.value = value
        }
        addRecord(record)
        val result = evaluator.invoke()
        val stringResult = result.toString()
        val resultRecord = prebuildRecord(MonitorAction.Result, parameter).also {
            it.value = stringResult
        }
        addRecord(resultRecord)
        return result
    }

    fun records(): List<Record>{
       return healthJournal.entries.lastOrNull()?.value?:emptyList<Record>()
    }

    fun records(phase:LifecyclePhase): List<Record>{
        return healthJournal[phase]?.toList()?:emptyList()
    }

    fun phaseReport(phase:LifecyclePhase): String{
        val records = records(phase)
        val recordsStr: String = records.joinToString(SpecialChars.NewLine.char) {
            it.formattedString
        }
        val phaseStr = "[${phase.name} of ${source.completeName.colorize(Colour.YELLOW)}] ${SpecialChars.NewLine.char}${recordsStr}"
        return phaseStr
    }

    fun print(phase:LifecyclePhase){
        phaseReport(phase)
    }

    fun report(): String{
        val phases =  healthJournal.keys.joinToString(separator = SpecialChars.NewLine.char.repeat(2)) {
            phaseReport(it)
        }
        val report = "${Colour.makeOfColour(Colour.BLUE, "Activity report")} for (${source.completeName}) ${SpecialChars.NewLine}$phases"
        return  report
    }

    fun print(){
       println(report())
    }


    fun ifWillCrash(parameter: String, predicate:T.()-> Boolean){
        val result = predicate.invoke(source)
        if(result){
           val record = prebuildRecord(MonitorAction.TestedParameter, parameter).also {
                it.message = "Crash is inevitable. Printing report".colorize(Colour.RED)
            }
            prepareForCrash(record)
        }else{
            prebuildRecord(MonitorAction.TestedParameter, parameter)
        }
    }


}
package po.test.misc.data

import org.junit.jupiter.api.Test
import po.misc.data.PrintableBase
import po.misc.data.console.DebugTemplate
import po.misc.data.console.PrintableTemplate
import po.misc.data.helpers.emptyOnNull
import po.misc.data.json.ElapsedTimeProvider
import po.misc.data.json.IntDefaultProvider
import po.misc.data.json.JasonStringSerializable
import po.misc.data.json.JsonDelegate
import po.misc.data.json.JsonDescriptor
import po.misc.data.json.JsonObjectDelegate
import po.misc.data.json.NanoTimeProvider
import po.misc.data.json.StringDefaultProvider
import po.misc.data.json.TrimmedQuotedStringProvider
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.colorize
import po.misc.data.templates.matchTemplate
import po.misc.data.templates.templateRule
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased
import po.misc.interfaces.asIdentifiable
import po.misc.interfaces.toValueBased
import po.misc.time.ExecutionTimeStamp
import kotlin.test.assertTrue

class TestJsonParser {

    data class TaskDataLocal(
        val nestingLevel: Int,
        val taskName: String,
        val timeStamp : ExecutionTimeStamp,
        val message: String,
        val severity: Int,
    ): PrintableBase<TaskDataLocal>(), JasonStringSerializable{

        override val self: TaskDataLocal = this

        override val itemId: ValueBased= toValueBased(0)
        override val emitter : Identifiable = asIdentifiable(taskName, "TestJsonParser")

        companion object {

            object TimeStampJson : JsonDescriptor<ExecutionTimeStamp>(){
                val nestingLevel :  Long  by JsonDelegate(ExecutionTimeStamp::startTime, NanoTimeProvider)
                val endTime :  Long  by JsonDelegate(ExecutionTimeStamp::endTime, NanoTimeProvider)
                val elapsed :  Float  by JsonDelegate( ExecutionTimeStamp::elapsed, ElapsedTimeProvider)
            }

            object TaskDataJson : JsonDescriptor<TaskDataLocal>(){
               val nestingLevel : Int by JsonDelegate(TaskDataLocal::nestingLevel, IntDefaultProvider)
               val taskName : String by JsonDelegate(TaskDataLocal::taskName,  TrimmedQuotedStringProvider)
               val timeStamp: String by JsonObjectDelegate<TimeStampJson, String>(TimeStampJson::jsonString,
                   StringDefaultProvider)
               val message : String by JsonDelegate( TaskDataLocal::message, StringDefaultProvider)
               val severity : Int by JsonDelegate(TaskDataLocal::severity, IntDefaultProvider)
            }


            val nestingFormatter: TaskDataLocal.() -> String = {
                matchTemplate(
                    templateRule(nestingLevel.toString()) { nestingLevel > 0 },
                    templateRule("Root ".colorize(Colour.GREEN)) { nestingLevel == 0 }
                )
            }

            val prefix: TaskDataLocal.(auxMessage: String) -> String = { auxMessage ->
                "${Colour.makeOfColour(Colour.BLUE, "[${auxMessage}")}  ${nestingFormatter(this)}" +
                        "${taskName} | ${emitter.componentName} @ $currentTime".colorize(Colour.BLUE)
            }

            val messageFormatter: TaskDataLocal.() -> String = {
                matchTemplate(
                    templateRule(message) { severity == 0 },
                    templateRule(message.colorize(Colour.YELLOW)) { severity == 1 },
                    templateRule(message.colorize(Colour.RED)) { severity == 2 }
                )
            }

            val Header: PrintableTemplate<TaskDataLocal> = PrintableTemplate {
                SpecialChars.NewLine.char + prefix.invoke(this, "Start") + "${emitter.componentName.emptyOnNull("by ")}]".colorize(Colour.BLUE)
            }

            val Footer: PrintableTemplate<TaskDataLocal> = PrintableTemplate {
                prefix.invoke(this, "Stop") +
                        " | $currentTime] Elapsed: ${timeStamp.elapsed}".colorize(Colour.BLUE)
            }

            val Message: PrintableTemplate<TaskDataLocal> = PrintableTemplate {
                "${prefix.invoke(this, "")} ${messageFormatter.invoke(this)}"
            }

            val Debug: DebugTemplate<TaskDataLocal> = DebugTemplate {
                "${prefix.invoke(this, "")} ${message.colorize(Colour.GREEN)}"
            }
        }
    }

    data class ValidationRep(
        private  var validationName: String,
    ): PrintableBase<ValidationRep>(), JasonStringSerializable
    {

        override val itemId : ValueBased = toValueBased(0)
        override val emitter: Identifiable = asIdentifiable("ValidationReport", "ValidationReport")

        override val self: ValidationRep = this


    }


    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun `To json conversion`(){

        val task = TaskDataLocal(
            nestingLevel = 0,
            taskName = "TestTask",
            timeStamp = ExecutionTimeStamp("task", 1.toString()),
            message = "All systems go",
            severity = 0
        )

        //val map = PrintableBase.Companion.firstRun<TaskDataLocal>()
        val report1 = ValidationRep("UserValidation")
        val report2 = ValidationRep("UserValidation")
        val report3 = ValidationRep("UserValidation")
        val report4 = ValidationRep("UserValidation")

        task.addChild(report1)
        task.addChild(report2)
        task.addChild(report3)
        task.addChild(report4)


        val output = TaskDataLocal.Companion.TaskDataJson.serialize(task)

        println(output)
        assertTrue (output.contains("TestTask"), "Output does not contain TestTask")
        assertTrue(output.contains("UserValidation"), "Output does not contain UserValidation")
        assertTrue(output.contains("FAILED"), "Output does not contain FAILED")

    }


}
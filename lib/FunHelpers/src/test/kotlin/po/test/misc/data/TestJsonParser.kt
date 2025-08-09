package po.test.misc.data

import org.junit.jupiter.api.Test
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableTemplate
import po.misc.data.helpers.emptyOnNull
import po.misc.data.json.IntDefaultProvider
import po.misc.data.json.JasonStringSerializable
import po.misc.data.json.JsonDelegate
import po.misc.data.json.JsonDescriptor
import po.misc.data.json.StringDefaultProvider
import po.misc.data.json.TrimmedQuotedStringProvider
import po.misc.data.json.jsonDelegatePart
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.printable.companion.nextLine
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.colorize
import po.misc.data.templates.matchTemplate
import po.misc.data.templates.templateRule
import po.misc.time.ExecutionTimeStamp


import kotlin.test.assertTrue

class TestJsonParser {

    data class TaskDataLocal(
        val contextName: String,
        val nestingLevel: Int,
        val taskName: String,
        val timeStamp: ExecutionTimeStamp,
        val message: String,
        val severity: Int,
    ) : PrintableBase<TaskDataLocal>(this), JasonStringSerializable {

        override val self: TaskDataLocal = this

        override fun toJson(): String {
            return descriptor.serialize(this)
        }

        companion object : PrintableCompanion<TaskDataLocal>({TaskDataLocal::class}){

        class Descriptor: JsonDescriptor<TaskDataLocal>(){
            val nestingLevel: Int by JsonDelegate(TaskDataLocal::nestingLevel, IntDefaultProvider)
            val taskName: String by JsonDelegate(TaskDataLocal::taskName, TrimmedQuotedStringProvider)
            val timeStamp : String  by  jsonDelegatePart<JsonDescriptor<TaskDataLocal>,  ExecutionTimeStamp, TaskDataLocal>(
                TaskDataLocal::timeStamp,
                ExecutionTimeStamp::startTime,
                ExecutionTimeStamp::endTime,
                ExecutionTimeStamp::elapsed,
            )
            val message: String by JsonDelegate(TaskDataLocal::message, StringDefaultProvider)
            val severity: Int by JsonDelegate(TaskDataLocal::severity, IntDefaultProvider)
        }
         val descriptor =  Descriptor()

            val nestingFormatter: TaskDataLocal.() -> String = {
                matchTemplate(
                    templateRule(nestingLevel.toString()) { nestingLevel > 0 },
                    templateRule("Root ".colorize(Colour.GREEN)) { nestingLevel == 0 }
                )
            }

            val prefix: TaskDataLocal.(auxMessage: String) -> String = { auxMessage ->
                "${Colour.makeOfColour(Colour.BLUE, "[${auxMessage}")}  ${nestingFormatter(this)}" +
                        "$taskName | @ $currentTime".colorize(Colour.BLUE)
            }

            val messageFormatter: TaskDataLocal.() -> String = {
                matchTemplate(
                    templateRule(message) { severity == 0 },
                    templateRule(message.colorize(Colour.Yellow)) { severity == 1 },
                    templateRule(message.colorize(Colour.RED)) { severity == 2 }
                )
            }

            val Footer = createTemplate {
                nextLine{
                    "${prefix} ${ timeStamp.endTime}  Elapsed: ${timeStamp.elapsed}".colorize(Colour.BLUE)
                }
            }

            val Header = createTemplate {
                nextLine {
                    SpecialChars.NewLine.char + prefix.invoke(
                        this,
                        "Start"
                    ) + "${contextName.emptyOnNull("by ")}]".colorize(Colour.BLUE)
                }
            }

            val Message = createTemplate {
                nextLine {
                    "${prefix.invoke(this, "")} ${messageFormatter.invoke(this)}"
                }
            }

            val Debug = createTemplate {
                nextLine {
                    "${prefix.invoke(this, "")} ${message.colorize(Colour.GREEN)}"
                }
            }
        }
    }

    data class ValidationRep(
        var validationName: String
    ): PrintableBase<ValidationRep>(this), JasonStringSerializable {
        override val self: ValidationRep = this
        override fun toJson(): String {
            return validationReportDescriptor.serialize(this)
        }

        companion object : PrintableCompanion<ValidationRep>({ValidationRep::class}) {
            class ValidationReportDescriptor: JsonDescriptor<ValidationRep>(){
                val validationName: String by JsonDelegate<ValidationRep, String>(ValidationRep::validationName, StringDefaultProvider)
            }
            val validationReportDescriptor = ValidationReportDescriptor()
            val Main = createTemplate {
                nextLine {
                    validationName
                }
            }
        }
    }


    fun `To json conversion`() {

        val task = TaskDataLocal(
            contextName = "SomeContext",
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
        task.addArbitraryRecord(report1)
        task.addArbitraryRecord(report2)
        task.addArbitraryRecord(report3)
        task.addArbitraryRecord(report4)

        val output = TaskDataLocal.descriptor.serialize(task)

        println(output)
        assertTrue(output.contains("TestTask"), "Output does not contain TestTask")
        assertTrue(output.contains("All systems go"), "Output does not contain message")

    }

    @Test
    fun `To json conversion as as array`() {

        val rootTask = TaskDataLocal(
            contextName = "Some name",
            nestingLevel = 0,
            taskName = "RootTask",
            timeStamp = ExecutionTimeStamp("task", 1.toString()),
            message = "Thought I was born on the roof",
            severity = 0
        )

        val tasks: MutableList<TaskDataLocal> = mutableListOf()
        for (i in 1..10) {
            val task = TaskDataLocal(
               contextName = "Some name",
                nestingLevel = i,
                taskName = "TestTask",
                timeStamp = ExecutionTimeStamp("task_${i}", i.toString()),
                message = "All systems go",
                severity = 0
            )
            tasks.add(task)
        }

        tasks.forEach { rootTask.addArbitraryRecord(it) }
        rootTask.toJson()
    }
}
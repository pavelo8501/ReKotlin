package po.test.misc.data

import org.junit.jupiter.api.Test
import po.misc.context.CTX
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableTemplate
import po.misc.data.helpers.emptyOnNull
import po.misc.data.json.IntDefaultProvider
import po.misc.data.json.JasonStringSerializable
import po.misc.data.json.JsonDelegate
import po.misc.data.json.JsonDescriptor
import po.misc.data.json.StringDefaultProvider
import po.misc.data.json.TrimmedQuotedStringProvider
import po.misc.data.json.jsonDelegatePart
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.colorize
import po.misc.data.templates.matchTemplate
import po.misc.data.templates.templateRule
import po.misc.context.asContext
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
    ) : PrintableBase<TaskDataLocal>(Message), JasonStringSerializable {

        override val self: TaskDataLocal = this

        override fun toJson(): String {
            return  serialize(this)
        }

        companion object : JsonDescriptor<TaskDataLocal>() {

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
                    templateRule(message.colorize(Colour.YELLOW)) { severity == 1 },
                    templateRule(message.colorize(Colour.RED)) { severity == 2 }
                )
            }

            val Header: PrintableTemplate<TaskDataLocal> = PrintableTemplate() {
                SpecialChars.NewLine.char + prefix.invoke(
                    this,
                    "Start"
                ) + "${contextName.emptyOnNull("by ")}]".colorize(Colour.BLUE)
            }

            val Footer: PrintableTemplate<TaskDataLocal> = PrintableTemplate() {
                prefix.invoke(this, "Stop") +
                        " | $currentTime] Elapsed: ${timeStamp.elapsed}".colorize(Colour.BLUE)
            }

            val Message: PrintableTemplate<TaskDataLocal> = PrintableTemplate() {
                "${prefix.invoke(this, "")} ${messageFormatter.invoke(this)}"
            }

            val Debug: PrintableTemplate<TaskDataLocal> = PrintableTemplate() {
                "${prefix.invoke(this, "")} ${message.colorize(Colour.GREEN)}"
            }
        }
    }

    data class ValidationRep(
        var validationName: String
    ): PrintableBase<ValidationRep>(Main), JasonStringSerializable {
        override val self: ValidationRep = this
        override fun toJson(): String {
            return serialize(this)
        }

        companion object :JsonDescriptor<ValidationRep>() {
            val validationName: String by JsonDelegate<ValidationRep, String>(ValidationRep::validationName, StringDefaultProvider)
            val Main  : PrintableTemplate<ValidationRep> = PrintableTemplate()
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

        task.addChild(report1)
        task.addChild(report2)
        task.addChild(report3)
        task.addChild(report4)

        val output = TaskDataLocal.serialize(task)

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

        rootTask.addChildren(tasks)
        rootTask.toJson()
    }
}
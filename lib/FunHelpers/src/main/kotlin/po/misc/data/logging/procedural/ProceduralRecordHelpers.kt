package po.misc.data.logging.procedural

import po.misc.data.logging.StructuredLoggable
import po.misc.data.output.output
import po.misc.data.strings.stringify
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize


fun ProceduralRecord.printProceduralTree(
    includeMessages: Boolean = false,
    nestingLevel: Int = 0
){
    fun outputMessages(level: Int){
           val indention = "--".repeat(level)
           logRecords.output("$indention Messages: ", Colour.Blue){
               it.stringify(indention,  Colour.Cyan).toString()
           }
    }
    if(nestingLevel == 0){
        "PrintProceduralTree".output(Colour.MagentaBright)
        "Procedural $text".output(Colour.Cyan)
        if(includeMessages){ outputMessages(nestingLevel) }
    }else{
        "Procedural $text".output("--".repeat(nestingLevel), Colour.Cyan)
        if(includeMessages){ outputMessages(nestingLevel) }
    }
    proceduralEntries.forEach {
        val thisLevel = nestingLevel + 1
        it.records.forEach { procedural->
            procedural.printProceduralTree(includeMessages, thisLevel)
        }
    }
}


fun StructuredLoggable.printStructuredTree(
    nestingLevel: Int = 0
){
    if(nestingLevel == 0){
        "PrintStructuredTree".output(Colour.MagentaBright)
         output()
    }else{
        output("--".repeat(nestingLevel))
    }
    val thisLevel = nestingLevel + 1
    getRecords().forEach {loggable->
        when(loggable){
            is StructuredLoggable -> loggable.printStructuredTree(thisLevel)
            else -> loggable.output()
        }
    }
}
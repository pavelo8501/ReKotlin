package po.lognotify.notification.models

import po.misc.collections.StaticTypeKey
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.types.token.TypeToken


/**
 * Defines the verbosity level of console output during task execution.
 * This enum is used to control how much information is printed to the console,
 * ranging from full output to selective muting of specific types of messages.
 */
enum class ConsoleBehaviour{

    /**
     * Print all output including task headers, events, and info logs.
     * This is the most verbose mode.
     */
    FullPrint,

    /**
    * Suppress all console output completely.
    * No headers, footers, or events will be printed.
    */
    Mute,

    /**
     * Suppress task headers and footers, but still allow event logs to be printed.
     * Useful for reducing clutter while retaining meaningful logs.
     */
    MuteNoEvents,

    /**
     * Suppress event logs with severity level `INFO`.
     * Warnings and errors are still printed. Task headers and footers are also printed.
     */
    MuteInfo,
}

data class NotifyConfig(
    var console : ConsoleBehaviour = ConsoleBehaviour.FullPrint,
    var debugAll:DebugOptions = DebugOptions.Listed
){

    enum class DebugOptions{
        Listed,
        DebugAll
    }

   // private val showDebugList : MutableList<DebugTemplate<*>> = mutableListOf()

    var debugWhiteList: MutableMap<Int, TypeToken<*>> = mutableMapOf()
        private set

   internal fun updateDebugWhiteList(whiteList: Map<Int, TypeToken<*>>){
        debugWhiteList.clear()
        debugWhiteList.putAll(whiteList)
    }

    fun allowDebug(vararg dataClasses: PrintableCompanion<*>){
        dataClasses.forEach {
            debugWhiteList[it.typeToken.hashCode()] = it.typeToken
        }
    }

    fun setConsoleBehaviour(behaviour: ConsoleBehaviour){
        console = behaviour
    }

//    fun inShowDebugList(debugTemplate: DebugTemplate<*>): Boolean{
//        return showDebugList.any { it == debugTemplate }
//    }

//    fun showDebugInfo(data: DebugTemplate<*>){
//        showDebugList.add(data)
//    }

}
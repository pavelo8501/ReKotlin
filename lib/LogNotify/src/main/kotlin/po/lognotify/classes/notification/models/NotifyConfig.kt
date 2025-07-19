package po.lognotify.classes.notification.models

import po.misc.collections.StaticTypeKey
import po.misc.data.printable.PrintableCompanion


data class NotifyConfig(
    var console : ConsoleBehaviour = ConsoleBehaviour.FullPrint,
    var debugAll:DebugOptions = DebugOptions.Listed
){
    enum class ConsoleBehaviour{
        FullPrint, //Print everything
        Mute, //Mute everything
        MuteNoEvents, //Mute task header/footer printout without any events
        MuteInfo, // Mute events with severity level info
    }

    enum class DebugOptions{
        Listed,
        DebugAll
    }


   // private val showDebugList : MutableList<DebugTemplate<*>> = mutableListOf()

    var debugWhiteList: MutableMap<Int, StaticTypeKey<*>> = mutableMapOf()
        private set

   internal fun updateDebugWhiteList(whiteList: Map<Int, StaticTypeKey<*>>){
        debugWhiteList.clear()
        debugWhiteList.putAll(whiteList)
    }

    fun allowDebug(vararg dataClasses: PrintableCompanion<*>){
        dataClasses.forEach {
            debugWhiteList[it.typeKey.hashCode()] = it.typeKey
        }
    }

//    fun inShowDebugList(debugTemplate: DebugTemplate<*>): Boolean{
//        return showDebugList.any { it == debugTemplate }
//    }

//    fun showDebugInfo(data: DebugTemplate<*>){
//        showDebugList.add(data)
//    }

}
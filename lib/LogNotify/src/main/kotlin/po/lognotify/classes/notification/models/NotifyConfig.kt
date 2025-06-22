package po.lognotify.classes.notification.models

import po.misc.data.console.DebugTemplate


enum class ConsoleBehaviour{
    FullPrint, //Print everything
    Mute, //Mute everything
    MuteNoEvents, //Mute task header/footer printout without any events
    MuteInfo, // Mute events with severity level info

}

data class NotifyConfig(
    var console : ConsoleBehaviour = ConsoleBehaviour.FullPrint,
){

    private val showDebugList : MutableList<DebugTemplate<*>> = mutableListOf()

    fun inShowDebugList(debugTemplate: DebugTemplate<*>): Boolean{
        return showDebugList.any { it == debugTemplate }
    }

    fun showDebugInfo(data: DebugTemplate<*>){
        showDebugList.add(data)
    }

}
package po.lognotify.classes.notification.models


enum class ConsoleBehaviour{
    FullPrint, //Print everything
    Mute, //Mute everything
    MuteNoEvents, //Mute task header/footer printout without any events
    MuteInfo, // Mute events with severity level info

}

data class NotifyConfig(
    var console : ConsoleBehaviour = ConsoleBehaviour.FullPrint,
){

}
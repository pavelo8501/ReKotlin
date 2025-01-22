package po.lognotify.eventhandler.interfaces

import po.lognotify.eventhandler.classes.StaticsHelper
import po.lognotify.eventhandler.models.Event
import po.lognotify.shared.enums.SeverityLevel

interface HandlerStatics {

    val moduleName: String

    companion object: StaticsHelper(){
        override lateinit var  moduleName: String
        fun init(moduleName: String) {
            this.moduleName = moduleName
        }
    }
}
package po.exposify.dto.enums

import po.misc.interfaces.Named


enum class Components(override val moduleName: String): Named {
    Service("Service"),
    DTOFactory("DTOFactory"),
    DaoService("DaoService"),
    Tracker("Tracker");
    override fun toString(): String {
        return moduleName
    }
}
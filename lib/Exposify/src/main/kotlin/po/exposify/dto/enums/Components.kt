package po.exposify.dto.enums




enum class Components(val moduleName: String){
    Service("Service"),
    DTOFactory("DTOFactory"),
    DaoService("DaoService"),
    Tracker("Tracker");
    override fun toString(): String {
        return moduleName
    }
}
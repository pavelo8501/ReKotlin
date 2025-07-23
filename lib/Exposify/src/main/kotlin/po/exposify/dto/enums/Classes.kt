package po.exposify.dto.enums




enum class Classes(val moduleName: String){
    CommonDTO("CommonDTO"),
    DTOClass("DTOClass"),
    RootClass("RootClass");

    override fun toString(): String {
        return moduleName
    }
}
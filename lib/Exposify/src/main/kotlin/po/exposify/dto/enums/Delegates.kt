package po.exposify.dto.enums



enum class Delegate(val moduleName: String){
    ResponsiveDelegate("ResponsiveDelegate"),
    AttachedForeignDelegate("AttachedForeignDelegate"),
    ParentDelegate("ParentDelegate"),
    RelationDelegate("RelationDelegate");

    override fun toString(): String {
        return moduleName
    }
}
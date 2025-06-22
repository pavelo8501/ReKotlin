package po.exposify.dto.enums

import po.misc.interfaces.Named

enum class Delegate(override val moduleName: String): Named{
    ResponsiveDelegate("ResponsiveDelegate"),
    AttachedForeignDelegate("AttachedForeignDelegate"),
    ParentDelegate("ParentDelegate"),
    RelationDelegate("RelationDelegate");

    override fun toString(): String {
        return moduleName
    }
}
package po.exposify.dto.enums

import po.misc.interfaces.Named


enum class Classes(override val moduleName: String):Named{
    CommonDTO("CommonDTO"),
    DTOClass("DTOClass"),
    RootClass("RootClass");

    override fun toString(): String {
        return moduleName
    }
}
package po.plugins.binder.auxiliary

import com.google.devtools.ksp.symbol.KSType

data class PropertyInfo (
    val type : KSType,
    val name : String,
    val typeStr : String,
)
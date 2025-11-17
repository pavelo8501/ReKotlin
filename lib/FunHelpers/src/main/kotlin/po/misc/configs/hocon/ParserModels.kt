package po.misc.configs.hocon

import com.typesafe.config.ConfigOrigin

data class NowParsing(
    val resourceName: String,
    val url:  java.net.URL,
    val description: String,
    val lineNumber: Int
){
    constructor(origin: ConfigOrigin):this(origin.resource(), origin.url(), origin.description(), origin.lineNumber())
    val parsing: String get() = "$resourceName Line: $lineNumber"

}
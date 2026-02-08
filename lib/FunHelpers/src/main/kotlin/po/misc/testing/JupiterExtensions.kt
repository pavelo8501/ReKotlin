package po.misc.testing

import org.opentest4j.AssertionFailedError
import po.misc.data.styles.SpecialChars
import po.misc.reflection.displayName
import kotlin.reflect.KProperty0


fun <T> T.assertBlock(heading:String, assertions: T.() -> Unit){
    try {
        assertions.invoke(this)
    }catch (th: Throwable){
        when(th){
            is AssertionFailedError -> {
                val withHeading = "While asserting block: $heading${SpecialChars.NEW_LINE}${th.message}"
                throw AssertionFailedError(withHeading, th)
            }
            else -> throw th
        }
    }
}

fun <V> assertBlock(prop: KProperty0<V>, assertions: V.() -> Unit){
    val blockName = prop.displayName
    val value = prop.get()
    try {
        assertions.invoke(value)
    }catch (th: Throwable){
        when(th){
            is AssertionFailedError -> {
                val withHeading = "While asserting block: $blockName${SpecialChars.NEW_LINE}${th.message}"
                throw AssertionFailedError(withHeading, th)
            }
            else -> throw th
        }
    }
}
package po.misc.context

import po.misc.context.models.IdentityData
import kotlin.reflect.typeOf


fun CTX.identify(message: String, block: IdentityData.() -> Unit) {
    block.invoke(IdentityData(this.identity.toString(), message))
}

inline fun <reified T : Any> CTX.getResolved(noinline block: T.() -> Unit) {
    (this as? T)?.let { block(it) }
}
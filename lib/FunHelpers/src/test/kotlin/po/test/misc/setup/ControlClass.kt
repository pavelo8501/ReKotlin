package po.test.misc.setup

import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asContext

internal interface TestLogger  : CTX

open class ControlClass(
    val property1: String = "Property1<String>",
    val property2: Int = 10,
    val property3: String = "Property2<String>",
): CTX {

    override val identity: CTXIdentity<out CTX> = asContext()
}


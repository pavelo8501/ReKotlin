package po.test.misc.setup

import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity

internal interface TestLogger  : CTX

open class ControlClass(
    var property1: String = "Property1<String>",
    var property2: Int = 10,
    var property3: String = "Property2<String>",
): CTX {

    override val identity: CTXIdentity<out CTX> = asIdentity()
}


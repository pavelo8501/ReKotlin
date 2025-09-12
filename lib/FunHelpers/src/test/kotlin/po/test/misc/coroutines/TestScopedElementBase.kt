package po.test.misc.coroutines

import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.coroutines.ScopedElementBase
import kotlin.coroutines.CoroutineContext

class TestScopedElementBase {

    class TestElement() : ScopedElementBase<TestElement>() {
        override val identity: CTXIdentity<TestElement> = asIdentity()
        override val key: CoroutineContext.Key<TestElement> get() = Key
        companion object Key : CoroutineContext.Key<TestElement>
    }


}
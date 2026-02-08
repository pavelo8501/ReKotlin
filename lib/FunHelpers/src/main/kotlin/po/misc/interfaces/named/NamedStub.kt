package po.misc.interfaces.named

import org.junit.jupiter.api.DisplayName
import po.misc.data.text_span.TextSpan


internal class NamedStub(
    override val name: String,
): NamedComponent


internal fun String.asNamed(): NamedComponent{
    return NamedStub(this)
}

internal fun Named.asNamedComponent(): NamedComponent{
    if(this is NamedComponent){
        return this
    }
    return NamedStub(name)
}


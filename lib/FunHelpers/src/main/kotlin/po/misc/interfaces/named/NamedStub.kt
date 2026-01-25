package po.misc.interfaces.named


internal class NamedStub(
    override val name: String,
): Named

internal fun String.asNamed(): Named{
    return NamedStub(this)
}
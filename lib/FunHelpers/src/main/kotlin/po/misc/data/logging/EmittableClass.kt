package po.misc.data.logging

interface EmittableClass: LogEmitter {
    fun warn(message: String)
    fun info(message: String)
    fun error(message: String)
}
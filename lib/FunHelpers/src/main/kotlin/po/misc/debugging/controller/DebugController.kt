package po.misc.debugging.controller


class DebugController<R: Any>(
    val result : R
) {

    var debugLambda: (Any.()-> Any)? = null
}



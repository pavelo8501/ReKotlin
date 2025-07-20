package po.misc.functions.dsl

import po.misc.functions.containers.ResponsiveContainer


open class DSLProvider<T: Any,  R: Any>(
    override val function: (T) -> R
): ResponsiveContainer<T, R>() {

    override val identifiedAs: String get() = "DSLProvider<T, R>"

    private var ownResult: R? = null
    override val result: R get() = resultBacking ?: ownResult!!

    override fun trigger(): R {
        return valueBacking?.let {
            val result = function.invoke(it)
            ownResult = result
            supplyResultBacking(result)
        } ?: run {
            val result = function.invoke(value)
            ownResult = result
            supplyResultBacking(result)
        }
    }
}

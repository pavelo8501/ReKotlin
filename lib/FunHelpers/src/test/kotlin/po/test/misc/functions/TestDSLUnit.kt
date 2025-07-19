package po.test.misc.functions

import org.junit.jupiter.api.Test
import po.misc.data.styles.SpecialChars
import po.misc.functions.containers.DSLProvider
import po.misc.functions.models.ContainerMode
import po.test.misc.setup.ControlClass


class TestDSLUnit() {


    interface Builder<T : Any, R : Any> {
        val receiver: T
        fun next(block: () -> R)
    }

//    interface Adapter<R : Any> {
//
//    }

    class DSLContainer<T : Any, R : Any>(
        internal val receiver: T
    ) {
        var id: Int = 1
        val dslBlocks: MutableList<DSLProvider<T, R>> = mutableListOf()
        val containers: MutableList<DSLContainer<*, R>> = mutableListOf()
        private fun newBlock(lambda: T.() -> R): DSLProvider<T, R> {
            return DSLProvider(receiver,  lambda).apply { containerMode = ContainerMode.Verbose }
        }

        @PublishedApi
        internal fun <T2 : Any> newContainer(receiver: T2): DSLContainer<T2, R> {
            val newContainer = DSLContainer<T2, R>(receiver)
            newContainer.id = containers.size + 2

            containers.add(newContainer)
            return newContainer
        }

        fun next(block: T.() -> R) {
            val dslBlock = newBlock(block)
            dslBlock.onChanged {
                println("New value update from hook ${it.newValue} ")
            }
            dslBlocks.add(dslBlock)
        }

        inline fun <reified T2 : Any> with(receiver2: T2, block: DSLContainer<T2, R>.() -> Unit): DSLContainer<T2, R> {
            return newContainer<T2>(receiver2).apply(block)
        }

        fun collectAllResults(): List<R> {
            val selfResults = dslBlocks.map { it.trigger() }
            val childResults = containers.flatMap { it.collectAllResults() }
            return selfResults + childResults
        }

        fun computeResult(adapterFn: (List<R>) -> R): R {
            val result = collectAllResults()
            return adapterFn.invoke(result)
        }

        override fun toString(): String = "DSLContainer<${receiver::class.simpleName}>#$id"
    }

    fun <T : Any, R : Any> build(receiver: T, block: DSLContainer<T, R>.() -> Unit): DSLContainer<T, R> {
        return DSLContainer<T, R>(receiver).apply(block)
    }

    class SubControlClass(val subProperty: String = "subProperty<String>")

    class DerivedControl(
        val subControlClass: SubControlClass = SubControlClass()
    ) : ControlClass()

    @Test
    fun `Test DSL Builder`() {
        val controlClass = DerivedControl()
        val dslContainer = build<DerivedControl, String>(controlClass) {
            next { property3 }
            next { property1 }
            with(receiver.subControlClass) {
                next { subProperty }
            }
        }
        val transformedResult = dslContainer.computeResult {
            it.joinToString(separator = SpecialChars.NewLine.char) {
                it
            }
        }
        println(transformedResult)
    }
}

//class Adapter<V: Any, R: Any?>(
//    val lambda:(V)-> R
//): ResponsiveContainer<V, R?>() {
//
//    private var resultBacking: R? = null
//    override val result: R?  get() = resultBacking
//
//    /**
//     * Executes the transformation lambda using the current input value if available.
//     * @return The transformed result, or null if no input was provided.
//     */
//    override fun trigger(): R? {
//        valueBacking?.let {
//            resultBacking = lambda.invoke(it)
//        }?:run {
//            println("Value parameter not provided")
//            null
//        }
//        return resultBacking
//    }
//
////    override fun provideValue(value: V) {
////        valueBacking = value
////    }
//}


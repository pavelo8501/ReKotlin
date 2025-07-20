package po.misc.functions.dsl

import po.misc.functions.dsl.DSLContainer


fun <T : Any, R : Any>  DSLBuilder<T,R>.dslBuilder(block: DSLContainer<T, R>.() -> Unit): DSLContainer<T, R> {

    this.dslContainer.block()

    val container = DSLContainer<T, R>(block)
    container.build()
    return container
}


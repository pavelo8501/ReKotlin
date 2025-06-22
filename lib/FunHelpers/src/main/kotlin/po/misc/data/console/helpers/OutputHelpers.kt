package po.misc.data.console.helpers

class VerticalMarginBuilder {
    private val content = mutableListOf<String>()

    operator fun String.unaryPlus() {
        content.add(this)
    }
    fun render(): String = content.joinToString("\n")
}

fun withVerticalMargin(linesBefore: Int = 1, linesAfter: Int = 1, block: () -> Unit) {
    repeat(linesBefore) { println() }
    block()
    repeat(linesAfter) { println() }
}

fun printWithVerticalMargin(text: String, linesBefore: Int = 1, linesAfter: Int = 1) {
    repeat(linesBefore) { println() }
    println(text)
    repeat(linesAfter) { println() }
}



fun VerticalMargin(
    linesBefore: Int = 1,
    linesAfter: Int = 1,
    block: VerticalMarginBuilder.() -> Unit
) {
    repeat(linesBefore) { println() }
    val builder = VerticalMarginBuilder().apply(block)
    println(builder.render())
    repeat(linesAfter) { println() }
}
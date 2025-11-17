package po.test.misc.data.printable.grouping

import org.junit.jupiter.api.Test
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.printable.grouping.PrintableProperty
import po.misc.data.printable.grouping.printableProperty
import po.misc.types.token.TypeToken
import kotlin.test.assertEquals


class TestPrintableProperty {

    internal class ChildPrintable(
        val childProperty: String = "childProperty"
    ): PrintableBase<ChildPrintable>(this){
        override val self = this
        companion object: PrintableCompanion<ChildPrintable>(TypeToken.create<ChildPrintable>())
    }

    internal class HostingPrintable(
        val ownProperty: String = "ownProperty"
    ): PrintableBase<HostingPrintable>(this){
        override val self = this

        var fromCallback = mutableListOf<ChildPrintable>()
        val child = PrintableProperty<ChildPrintable>(this, "child")
        val child2 = PrintableProperty<ChildPrintable>(this, "child2"){
            fromCallback.add(it)
        }
        val child3 = printableProperty<ChildPrintable>("child2")

        var child4PropName: String = ""

        val child4 by printableProperty{child: ChildPrintable ->
            child4PropName = name
        }

        companion object: PrintableCompanion<HostingPrintable>(TypeToken.create())

    }

    private fun createChild(text: String? = null): ChildPrintable{
       return text?.let {
            ChildPrintable(it)
        }?: ChildPrintable()
    }

    @Test
    fun `PrintableProperty usage case`(){
        val printable = HostingPrintable()
        printable.child.add(createChild())
        printable.child.add(createChild())
        assertEquals(2, printable.child.size)
    }

    @Test
    fun `PrintableProperty usage case with callback`(){
        val printable = HostingPrintable()
        printable.child2.add(createChild())
        printable.child2.add(createChild())
        assertEquals(2, printable.child2.size)
        assertEquals(2,  printable.fromCallback.size)
    }
    @Test
    fun `PrintableProperty created as delegate properly receives its name`(){
        val printable = HostingPrintable()
        printable.child4.add(createChild())
        assertEquals("child4", printable.child4PropName)

    }

}
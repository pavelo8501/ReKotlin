package po.test.misc.reflection

import org.junit.jupiter.api.Test
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.interfaces.named.NameValue

class TestPropertyMapper {

    enum class ID(override val value : Int): NameValue{
        CLASS_1(1)
    }

    data class SourceClass(
        val property1: String = "Property1",
        val property2 : Int = 10,
        val property3: Boolean = false, var sourceName: String = ""
    ): CTX{

        override val identity: CTXIdentity<out CTX> = asIdentity()

        override val contextName: String
            get() = TODO("Not yet implemented")
    }

    @Test
    fun `Property map`(){


    }

}
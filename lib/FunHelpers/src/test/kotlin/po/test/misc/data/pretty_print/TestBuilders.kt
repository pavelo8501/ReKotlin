package po.test.misc.data.pretty_print

import org.junit.jupiter.api.assertDoesNotThrow
import po.misc.data.pretty_print.buildPrettyGrid
import po.misc.types.token.TypeToken
import po.test.misc.data.pretty_print.setup.PrintableRecord
import kotlin.test.Test

class TestBuilders {

    private val records = listOf(PrintableRecord())
    private val record = PrintableRecord()
    private val typeToken = TypeToken.Companion<TestBuilders>()

    @Test
    fun `Grid builder does not throw`() {
        assertDoesNotThrow {
            buildPrettyGrid<TestBuilders> {}
        }
    }

    @Test
    fun `Value grid type builder does not throw`() {
        assertDoesNotThrow {
            buildPrettyGrid<TestBuilders> {
                buildGrid(TestBuilders::record) {}
            }
        }
    }

    @Test
    fun `Value grid list type builder does not throw`() {
        assertDoesNotThrow {
            buildPrettyGrid<TestBuilders> {
                buildListGrid(TestBuilders::records) {}
            }
        }
    }

    @Test
    fun `Row builder does not throw`(){
        assertDoesNotThrow {
            buildPrettyGrid<TestBuilders> {
                buildRow{}
            }
        }
    }
    @Test
    fun `Value Row builder does not throw`(){
        assertDoesNotThrow {
            buildPrettyGrid<TestBuilders> {
                buildRow(TestBuilders::record){}
            }
        }
    }
}
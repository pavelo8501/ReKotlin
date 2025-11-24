package po.test.misc.collections.reactive_list

import org.junit.jupiter.api.Test
import po.misc.collections.lambda_map.Lambda
import po.misc.collections.reactive_list.ReactiveList
import po.misc.context.tracable.TraceableContext
import po.misc.data.output.output
import po.misc.data.styles.Colour

class TestReactiveList : TraceableContext {


    @Test
    fun `ReactiveList usage`(){

        val lambda: Lambda<String, Unit> = Lambda {
            it.output(Colour.Blue)
        }
        val reactiveList = ReactiveList<String, Unit>(ReactiveList.Options(this), lambda)
        reactiveList.add("string_1")
    }

}
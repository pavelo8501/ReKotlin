package po.test.misc.data.logging

import po.misc.data.logging.LogEmitterClass
import kotlin.test.Test

class TestLogEmitter {


    @Test
    fun `Log emitter class`(){

        val emitter = LogEmitterClass(this)

        emitter.info("Some msg")



    }

}
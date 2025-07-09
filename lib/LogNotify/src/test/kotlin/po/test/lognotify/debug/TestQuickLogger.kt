package po.test.lognotify.debug

import org.junit.jupiter.api.Test
import po.lognotify.extensions.runTaskBlocking

class TestQuickLogger {



    @Test
    fun `DSL type logging get current task in context`(){

        runTaskBlocking("Task1"){


        }

    }

}
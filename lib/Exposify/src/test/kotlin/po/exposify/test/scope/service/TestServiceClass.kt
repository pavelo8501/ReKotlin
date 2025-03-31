package po.exposify.test.scope.service


import po.exposify.test.DatabaseTest
import po.exposify.test.setup.TestPage
import po.exposify.test.setup.TestPageDTO
import po.exposify.test.setup.pageModels
import kotlin.test.Test

class TestServiceClass : DatabaseTest() {

    @Test
    fun `service instantiates root dtos and insert table records`(){
        connectionContext?.let {
            it.service<TestPageDTO, TestPage>(TestPageDTO) {
                val pages = pageModels(2)
                update(pages)
            }
        }


      //  assertTrue(open)
    }
}
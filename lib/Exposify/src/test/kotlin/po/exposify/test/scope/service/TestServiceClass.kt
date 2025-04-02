package po.exposify.test.scope.service

import po.exposify.scope.service.enums.TableCreateMode
import po.exposify.test.DatabaseTest
import po.exposify.test.setup.TestPage
import po.exposify.test.setup.TestPageDTO
import po.exposify.test.setup.pageModelsWithSections
import kotlin.test.Test

class TestServiceClass : DatabaseTest() {

    @Test
    fun `service instantiates root dtos and insert table records`(){
        connectionContext?.let {
            it.service<TestPageDTO, TestPage>(TestPageDTO, TableCreateMode.FORCE_RECREATE) {
                val pages = pageModelsWithSections(pageCount = 1, sectionsCount = 2)
                update(pages)
                select()
            }
        }


      //  assertTrue(open)
    }
}
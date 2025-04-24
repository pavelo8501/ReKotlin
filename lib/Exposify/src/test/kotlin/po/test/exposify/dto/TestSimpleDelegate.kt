package po.test.exposify.dto


import po.exposify.extensions.castOrOperationsEx
import po.exposify.scope.service.enums.TableCreateMode
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.TestPageDTO
import po.test.exposify.setup.TestUser
import po.test.exposify.setup.pageModels
import kotlin.test.Test
import kotlin.test.assertEquals

class TestSimpleDelegate : DatabaseTest() {

//    @Test
//    fun `referenced property binding`(){
//        val user = TestUser("some_login", "name", "nomail@void.null", "******")
//        val pages = pageModels(quantity =  1, updatedBy = 1)
//
//        var assignedUserId : Long = 0
//        connectionContext?.let {connection->
//            connection.service(TestPageDTO, TableCreateMode.FORCE_RECREATE) {
//                val pageData = pages[0]
//                pageData.name = "initial"
//                val pageDTO =  update(pages[0]).getDTO().castOrOperationsEx<TestPageDTO>()
//                assertEquals("initial", pageDTO.name)
//            }
//        }
//    }

}
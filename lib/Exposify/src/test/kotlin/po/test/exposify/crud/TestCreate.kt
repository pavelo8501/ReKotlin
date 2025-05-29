package po.test.exposify.crud

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import po.auth.extensions.generatePassword
import po.exposify.dto.components.result.toResultList
import po.exposify.scope.sequence.extensions.sequence
import po.exposify.scope.service.enums.TableCreateMode
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.sectionsPreSaved


class TestCreate: DatabaseTest() {

    @Test
    fun `Created default records`()= runTest{

        startTestConnection {

            service(UserDTO, TableCreateMode.CREATE) {
                if (select().getDTO().count() == 0) {
                    val admin = User(0, "pablito", generatePassword("passwordinio"), "Pavel", "pavelo@inbox.lv")
                    update(admin)
                }
                sequence(UserDTO.SELECT) { handler ->
                    pick(handler.query).toResultList()
                }
            }

            service(PageDTO, TableCreateMode.CREATE) {
                val count = select().getDTO().count()
                if (count != 3) {
                   // truncate()
                    val defaultPages = listOf(Page(0, "home", 1), Page(0, "home", 2), Page(0, "home", 3))
                    defaultPages.forEach { it.sections.addAll(sectionsPreSaved(it.id)) }
                    update(defaultPages)
                }
            }
        }
    }

}
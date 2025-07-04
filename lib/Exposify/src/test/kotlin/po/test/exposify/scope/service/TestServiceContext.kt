package po.test.exposify.scope.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import po.exposify.scope.service.models.TableCreateMode
import po.lognotify.TasksManaged
import po.test.exposify.setup.ContentBlocks
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.Pages
import po.test.exposify.setup.Sections
import po.test.exposify.setup.Users
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.UserDTO


class TestServiceContext : DatabaseTest(), TasksManaged {


    @Test
    fun `Tables ForceRecreate work as expected`() {

        withConnection {

            assertDoesNotThrow {
                service(UserDTO, TableCreateMode.Create) {

                }

                service(PageDTO, TableCreateMode.Create) {

                }
                clearServices()

                service(UserDTO, TableCreateMode.ForceRecreate.withTables(Users, Pages, Sections, ContentBlocks)) {

                }
            }
        }
    }
}
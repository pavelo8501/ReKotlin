package po.test.exposify.dsl.crud

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import po.exposify.scope.launchers.pick
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.mocks.mockPages
import po.test.exposify.setup.mocks.mockedUser
import po.test.exposify.setup.mocks.newMockedSession
import po.test.exposify.setup.mocks.withSections
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Duration


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestDSLPick: DatabaseTest() {

    override val identity: CTXIdentity<out CTX> = asIdentity()

     companion object {
        @JvmStatic
        var updatedById: Long = 0

         @JvmStatic
         lateinit var persistedPages: List<Page>
    }

    @BeforeAll
    fun setup() {
        withConnection {
            service(UserDTO) {
                updatedById = update(mockedUser).dataUnsafe.id
            }
        }
        val pages = mockPages(updatedById, 1) {
            withSections(2)
        }
        withConnection {
            service(PageDTO) {
                persistedPages =  insert(pages).dataUnsafe
            }
        }
    }


    @Test
    fun `Pick statement`() = runTest(timeout = Duration.parse("600s")){

        val page = persistedPages.first()

        PageDTO.clearCachedDTOs()
        val result =  with(newMockedSession){
            pick(PageDTO, page.id)
        }

        val pickedPage = assertNotNull(result.dataUnsafe)
        assertEquals(page, pickedPage)

    }

}
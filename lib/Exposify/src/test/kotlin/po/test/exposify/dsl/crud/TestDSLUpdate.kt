package po.test.exposify.dsl.crud

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import po.exposify.scope.launchers.pick
import po.exposify.scope.launchers.update
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.SectionDTO
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.mocks.mockPages
import po.test.exposify.setup.mocks.mockedUser
import po.test.exposify.setup.mocks.newMockedSession
import po.test.exposify.setup.mocks.withSections
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Duration


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class TestDSLUpdate: DatabaseTest() {



    override val identity: CTXIdentity<TestDSLUpdate> = asIdentity()

    companion object {
        @JvmStatic
        var updatedById: Long = 0


        @JvmStatic
        var pageDtos: List<PageDTO> = mutableListOf()

    }

    @BeforeAll
    fun setup() {
        withConnection {
            service(UserDTO) {
                updatedById = update(mockedUser).dataUnsafe.id
            }
        }
        val pages = mockPages(updatedById, 1)

        withConnection {
            service(PageDTO) {
                pageDtos = insert(pages).dto
            }
        }
    }


    @Test
    @Order(1)
    fun `Update statement updates page and inserts additional sections`() = runTest(timeout = Duration.parse("600s")){

        val pageToUpdate = pageDtos.first()
        val pageDataModel = pageToUpdate.dataContainer.getValue(this)
        with(pageDataModel){
            withSections(2)
        }
       val result =  with(newMockedSession){
            update(PageDTO, pageDataModel)
        }

        val page = result.dataUnsafe
        assertEquals(2, page.sections.size)
        val lastSection = assertNotNull(page.sections.lastOrNull())

        val inputLastSection = assertNotNull(pageDataModel.sections.lastOrNull())
        assertEquals(inputLastSection.name, lastSection.name)
        assertEquals(inputLastSection.description, lastSection.description)
        assertEquals(inputLastSection.updatedBy, lastSection.updatedBy)
    }

    @Test
    @Order(2)
    fun `Update single in pick by id lambda`() = runTest(timeout = Duration.parse("600s")){


        val pickId = 1L

       val result = with(newMockedSession){
            pick(PageDTO, pickId){
                val firstSection = dataUnsafe.sections.first()
                firstSection.name = "updated_name"
                update(SectionDTO, firstSection)
            }
        }
        val section = assertNotNull(result.data)
        assertEquals("updated_name", section.name)
    }

    @Test
    @Order(3)
    fun `Update list in pick by id lambda`() = runTest(timeout = Duration.parse("600s")){
        val pickId = 1L
        val result = with(newMockedSession){
            pick(PageDTO, pickId){
                dataUnsafe.sections.forEachIndexed {index, value ->
                    value.name = "updated_name_${index}"
                }
                update(SectionDTO, dataUnsafe.sections)
            }
        }

        val sections = assertNotNull(result.data)
        val firstSection =   assertNotNull(sections.firstOrNull())
        assertEquals("updated_name_0", firstSection.name)
    }


}
package po.test.exposify.dsl.crud

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import po.auth.extensions.runWithSession
import po.exposify.scope.launchers.pick
import po.exposify.scope.launchers.select
import po.exposify.scope.launchers.update
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.test.exposify.setup.ClassData
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.ContentBlock
import po.test.exposify.setup.dtos.ContentBlockDTO
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.SectionDTO
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.mocks.mockPages
import po.test.exposify.setup.mocks.mockedUser
import po.test.exposify.setup.mocks.newMockedSession
import po.test.exposify.setup.mocks.withContentBlocks
import po.test.exposify.setup.mocks.withSections
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
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
        val pages = mockPages(updatedById, 3){index->
            langId = index+1
            withSections(2){
                withContentBlocks(2)
            }
        }

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
        val pageDataModel = pageToUpdate.dataContainer.getValue(this@TestDSLUpdate)
        with(pageDataModel){
            withSections(2)
        }
       val result =  with(newMockedSession){
            update(PageDTO, pageDataModel)
        }

        val page = result.dataUnsafe
        assertEquals(4, page.sections.size)
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

    @Test
    @Order(4)
    fun `Deep nested update after PickById and consequtive select`() = runTest(timeout = Duration.parse("600s")) {

        val page = pageDtos.first().dataContainer.getValue(this@TestDSLUpdate)
        val contentBlockToUpdate = page.sections.first().contentBlocks.first()
        val newContentBlock = ContentBlock(
            id = contentBlockToUpdate.id,
            name = contentBlockToUpdate.name,
            content = contentBlockToUpdate.content,
            tag = contentBlockToUpdate.tag,
            jsonLd = contentBlockToUpdate.jsonLd,
            langId = contentBlockToUpdate.langId,
            metaTags = contentBlockToUpdate.metaTags,
            sectionId = contentBlockToUpdate.sectionId,
            classList = listOf(ClassData(1, "UpdatedValue"))
        )

        contentBlockToUpdate.classList
        val session = newMockedSession
        val result = session.runWithSession {
            pick(PageDTO, page.id) {
                pick(SectionDTO, contentBlockToUpdate.sectionId) {
                    update(ContentBlockDTO, newContentBlock)
                }
            }
        }
        val persistedContentBlock = assertNotNull(result.data)
        val persistedClassItem = assertNotNull(persistedContentBlock.classList.firstOrNull())
        assertEquals("UpdatedValue", persistedClassItem.value)

        val pageSelectResult = session.runWithSession {
            select(PageDTO)
        }

        val firstPage = assertNotNull(pageSelectResult.data.firstOrNull())
        val firstSection =  assertNotNull(firstPage.sections.firstOrNull(), "Section list after select is empty")
        val firstContentBlock  =  assertNotNull(firstSection.contentBlocks.firstOrNull())
        assertEquals("UpdatedValue", firstContentBlock.classList.firstOrNull()?.value?:"None")
    }
}
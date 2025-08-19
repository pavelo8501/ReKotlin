package po.test.exposify.dto.components.bindings.relation_binder

import kotlinx.coroutines.test.runTest
import okio.Timeout
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import po.exposify.dto.components.bindings.helpers.newDTO
import po.exposify.dto.components.bindings.relation_binder.delegates.AttachedForeignDelegate
import po.exposify.dto.components.bindings.relation_binder.delegates.OneToManyDelegate
import po.exposify.dto.enums.Cardinality
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.PageEntity
import po.test.exposify.setup.SectionEntity
import po.test.exposify.setup.UserEntity
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.Section
import po.test.exposify.setup.dtos.SectionDTO
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.mocks.mockPages
import po.test.exposify.setup.mocks.mockedUser
import po.test.exposify.setup.mocks.withContentBlocks
import po.test.exposify.setup.mocks.withSections
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.time.Duration


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestRelationDelegates: DatabaseTest() {

    override val identity: CTXIdentity<out CTX> = asIdentity()


    companion object{
        @JvmStatic
        lateinit var user: UserDTO
    }

    @BeforeAll
    fun setup(){
        withConnection {
            service(UserDTO){
                user =  update(mockedUser).getDTOForced()
            }

            service(PageDTO){

            }
        }
    }

    @Test
    fun `Relation delegates key parameters are correct`(){

        val page = mockPages(updatedBy =  1, quantity = 1).first()
        val pageDTO = PageDTO.newDTO(page)

        assertEquals(1, pageDTO.bindingHub.relationDelegateMap.size)
        val delegate = assertNotNull(pageDTO.bindingHub.relationDelegateMap.values.firstOrNull())
        val resolvedDelegate =  assertIs<OneToManyDelegate<PageDTO, Page, PageEntity, SectionDTO, Section, SectionEntity>>(delegate)
        assertIs<SectionDTO.Companion>(resolvedDelegate.dtoClass)
        assertIs<PageDTO.Companion>(resolvedDelegate.hostingDTOClass)

        assertEquals(1, pageDTO.bindingHub.attachedForeignMap.size)
        val firstDelegate = assertNotNull(pageDTO.bindingHub.attachedForeignMap.values.firstOrNull())
        val attachedUserDelegate =  assertIs<AttachedForeignDelegate<PageDTO, Page, PageEntity, UserDTO, User, UserEntity>>(firstDelegate)
        assertIs<UserDTO.Companion>(attachedUserDelegate.dtoClass)
        assertIs<PageDTO.Companion>(resolvedDelegate.hostingDTOClass)

        assertEquals(0, pageDTO.bindingHub.parentDelegateMap.size)

    }

    @Test
    fun `Key functionality work as expected`() = runTest(timeout = Duration.parse("600s")){

        val page = mockPages(updatedBy =  1, quantity = 1 ){
            withSections(2){
                withContentBlocks(1)
            }
        }.first()

        val pageDTO = PageDTO.newDTO(page)
        pageDTO.saveDTO(PageDTO)

        assertEquals(1, pageDTO.bindingHub.relationDelegateMap.size)
        var relationsDelegate = assertNotNull(pageDTO.bindingHub.relationDelegateMap.values.firstOrNull())
        assertEquals(Cardinality.ONE_TO_MANY, relationsDelegate.cardinality)

        relationsDelegate =  assertIs<OneToManyDelegate<PageDTO, Page, PageEntity, SectionDTO, Section, SectionEntity>>(relationsDelegate)

        val sections = relationsDelegate.extractDataModels()
        assertEquals(2, sections.size)
        val lastSection = sections.last()
        assertIs<Section>(lastSection)

        val sectionEntities = relationsDelegate.extractEntities()

        assertEquals(2, sectionEntities.size)
        val lastSectionEntity = sectionEntities.last()
        assertIs<SectionEntity>(lastSectionEntity)


    }
}
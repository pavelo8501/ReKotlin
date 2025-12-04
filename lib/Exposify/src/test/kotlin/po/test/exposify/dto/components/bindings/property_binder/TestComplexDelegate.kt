package po.test.exposify.dto.components.bindings.property_binder

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import po.exposify.dto.components.bindings.helpers.newDTO
import po.exposify.dto.helpers.asDTO
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.mocks.mockPages
import po.test.exposify.setup.mocks.mockedUser
import kotlin.test.assertEquals

class TestComplexDelegate: DatabaseTest() {

    override val identity: CTXIdentity<TestComplexDelegate> = asIdentity()

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
    fun `Data can be updated outside of standard routine`() = runTest{

        val page = mockPages(updatedBy = user.id, quantity = 1).first()
        val pageDTO = PageDTO.newDTO(page)
        pageDTO.saveDTO(PageDTO)
        val asDTO = pageDTO.asDTO()
        val newName = "other_name"
        asDTO.name = newName
        asDTO.flush()
        val dataModel = asDTO.dataContainer.getValue(this@TestComplexDelegate)
        assertEquals(newName, dataModel.name)
    }
}
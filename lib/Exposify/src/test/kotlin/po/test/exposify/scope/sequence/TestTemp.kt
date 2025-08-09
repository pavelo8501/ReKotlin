package po.test.exposify.scope.sequence

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import po.auth.extensions.generatePassword
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestTemp : DatabaseTest() {

    override val identity: CTXIdentity<out CTX> = asIdentity()

    @BeforeAll
    fun `initial test`(){

        withConnection {
            service(UserDTO){
                update(User(id=0, login = "Login", hashedPassword = generatePassword("password"), name = "Pavel", email = "somemail"))
            }

            val stranicy: List<Page> = listOf(Page(id=0, name = "Stranica 1", 1, updatedBy =1),  Page(id=0, name =  "Stranica 2", 1, updatedBy = 1))
            service(PageDTO){
                update(stranicy)
            }
        }
    }


    @Test
    fun `Some test`(){

        val idStranicy = 2L
        var iskomajaSranica: Page? = null

        withConnection {
            service(PageDTO){
                iskomajaSranica = pickById(idStranicy).data
            }
        }
        val page = assertNotNull(iskomajaSranica)
        assertEquals("Stranica 2", page.name)

    }

}
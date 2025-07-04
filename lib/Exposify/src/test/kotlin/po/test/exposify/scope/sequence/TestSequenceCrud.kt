package po.test.exposify.scope.sequence

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import po.auth.extensions.createDefaultIdentifier
import po.auth.extensions.generatePassword
import po.auth.extensions.withSessionContext
import po.exposify.DatabaseManager
import po.exposify.dto.components.WhereQuery
import po.exposify.scope.connection.models.ConnectionInfo
import po.exposify.scope.sequence.extensions.runSequence
import po.exposify.scope.sequence.extensions.runSequence2
import po.exposify.scope.sequence.extensions.sequence
import po.exposify.scope.sequence.extensions.switchContext
import po.exposify.scope.service.enums.TableCreateMode
import po.lognotify.LogNotifyHandler
import po.lognotify.TasksManaged
import po.lognotify.classes.notification.models.ConsoleBehaviour
import po.lognotify.logNotify
import po.test.exposify.crud.TestSelect
import po.test.exposify.scope.session.TestSessionsContext
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.Users
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.Section
import po.test.exposify.setup.dtos.SectionDTO
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.pageModelsWithSections
import po.test.exposify.setup.pagesSectionsContentBlocks
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestSequenceCrud : DatabaseTest(),  TasksManaged {

    companion object{
        @JvmStatic
        var updatedById : Long = 0

        @JvmStatic
        val session = TestSessionsContext.SessionIdentity("0", "192.169.1.1")
    }

    @BeforeAll
    fun setup() {

        val loggerHandler: LogNotifyHandler = logNotify()
        loggerHandler.notifierConfig {
            console = ConsoleBehaviour.MuteNoEvents
        }
        val user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null"
        )
        withConnection {
            service(UserDTO, TableCreateMode.FORCE_RECREATE) {
                TestSelect.Companion.updatedById = update(user).getDataForced().id
            }
        }
    }

    fun jsonToSection(): Section{
        val jsonStr = """
            {"id": 1,
             "name": "moto_section",
             "description": "",
             "json_ld": "",
             "lang_id": 1,
             "updated": "2025-05-04T14:16:41.312453",
             "page_id": 1,
             "updated_by": 1,
              "content_blocks": [
                    {
                        "class_list": [{"key": 0, "value": "text-block"}],
                        "meta_tags": [{"type": 1,  "key": "",  "value": "text-block"}],
                        "id": 1,
                        "name": "moto_header",
                        "content": "<p>Enhance Workplace Safety with Medprofddddd</p>",
                        "tag": "h2",
                        "json_ld": "",
                        "lang_id": 1,
                        "section_id": 2
                    },
                    {
                        "class_list": [
                            {
                                "key": 0,
                                "value": "text-block"
                            },
                            {
                                "key": 1,
                                "value": "oooo"
                            }
                        ],
                        "meta_tags": [
                            {
                                "type": 1,
                                "key": "",
                                "value": "text-block"
                            }
                        ],
                        "id": 2,
                        "name": "moto_text",
                        "content": "<p>Mūsu visaptverošās veselības pārbaudes nodroddddšinssssa, ka jūsu uzņēmums atbilst visiem veselības noteikumiem, aizsargājot darbinieku labklājību. Mēs piedāvājam detalizētus pārskatus un praktiskus ieteikumus, lai savlaicīgi novērstu jebkādas veselības problēmas un veicinātu veselīgāku darba vidi.dddddd</p>",
                        "tag": "p",
                        "json_ld": "",
                        "lang_id": 1,
                        "section_id": 2
                    }
                ],
                "class_list": [
                    {
                        "key": 0,
                        "value": "text-block"
                    }
                ],
                "meta_tags": [
                    {
                        "type": 1,
                        "key": "",
                        "value": "text-block"
                    }
                ]
            }
        """.trimIndent()
        return Json.decodeFromString<Section>(jsonStr)
    }

    @Test
    fun `Update sequence as a DTO hierarchy child member`() = runTest {

        val pages =  pagesSectionsContentBlocks(pageCount = 1, sectionsCount = 1, contentBlocksCount = 2, updatedBy = updatedById)
        withConnection {
            service(PageDTO, TableCreateMode.CREATE) {
                update(pages)
                sequence(PageDTO.SELECT) {
                    switchContext(SectionDTO.UPDATE){ handler->
                        update(handler.inputData).toResultList()
                    }
                    select()
                }
            }
        }

        val result = runSequence2(SectionDTO.UPDATE, { PageDTO.switchQuery(1) }) {
            withData(jsonToSection())
        }.getData().first()




    }


    fun `Pick statement is processed in Sequence context`() = runTest {

        val loggerHandler: LogNotifyHandler = logNotify()
        loggerHandler.notifierConfig {
            console = ConsoleBehaviour.MuteNoEvents
        }

        val inputUser = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null"
        )

        withConnection{
            service(UserDTO){
                update(inputUser)
                sequence(UserDTO.PICK) { handler ->
                    pick(handler.query).toResultList()
                }
            }
        }

        var userFail: User? = null
        var userSuccess: User? = null
        withSessionContext(createDefaultIdentifier()) {
            userFail = runSequence(UserDTO.PICK) {
                withQuery {
                    WhereQuery(Users).equalsTo({ login }, "wrong")
                }
            }.getData().firstOrNull()
            userSuccess = runSequence(UserDTO.PICK) {
                withQuery {
                    WhereQuery(Users).equalsTo({ login }, inputUser.login)
                }
            }.getData().firstOrNull()
        }

        assertNull(userFail)
        val selectedUser = assertNotNull(userSuccess)
        assertEquals(selectedUser.name, inputUser.name)
        assertEquals(selectedUser.login, inputUser.login)
    }


    fun `test run n a real db with sequence select`() = runTest{

        val connectionInfo = ConnectionInfo(host ="0.0.0.0", port ="5432", dbName = "medprof_postgres", user = "django-api", pwd = "django-api_usrPWD12")
        DatabaseManager.openConnection(connectionInfo).service(PageDTO){
            sequence(PageDTO.SELECT) {
                select()
            }
        }
       val result = runSequence(PageDTO.SELECT).getData()

        assertTrue(result.isNotEmpty())
    }
}
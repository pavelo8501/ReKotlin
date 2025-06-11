package po.test.exposify.dto

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import po.exposify.exceptions.InitException
import po.exposify.scope.service.enums.TableCreateMode
import po.misc.exceptions.ManagedException
import po.test.exposify.dto.TestDTOTracker.Companion.updatedById
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.pageModelsWithSections
import po.test.exposify.setup.pagesSectionsContentBlocks

class TestDTOConfiguration : DatabaseTest() {


    @Test
    fun `Validator reports fails when missing init`(){
       val pages = pageModelsWithSections(pageCount = 1, sectionsCount = 1, updatedBy = 0)
        val exception = assertThrows<InitException> {
            startTestConnection {
                service(PageDTO){
                    update(pages)
                }
            }
        }
        throw exception
    }

    @Test
    fun `Happy path validator reports all green`(){


        startTestConnection{
            service(UserDTO, TableCreateMode.FORCE_RECREATE) {
                updatedById = update(user).getDataForced().id
            }
        }

    }

}
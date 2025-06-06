package po.test.exposify.dto

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import po.exposify.exceptions.InitException
import po.misc.exceptions.ManagedException
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.pageModelsWithSections
import po.test.exposify.setup.pagesSectionsContentBlocks

class TestDTOConfiguration : DatabaseTest() {


    @Test
    fun `Dto configuration routine executed completely`(){

        val pages = pageModelsWithSections(pageCount = 1, sectionsCount = 1, updatedBy = 0)

        val exception = assertThrows<ManagedException> {
            startTestConnection {
                service(PageDTO){
                    update(pages)
                }
            }
        }

    }

}
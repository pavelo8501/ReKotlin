package po.exposify.test.dto

import org.junit.jupiter.api.Test
import po.exposify.test.DatabaseTest
import po.exposify.test.setup.TestSectionDTO
import po.exposify.test.setup.sectionModel

class TestClassDTO : DatabaseTest() {



    @Test
    fun initialization(){




        val sectionDto = TestSectionDTO(sectionModel("test_section"))





        val a = 10

    }

}
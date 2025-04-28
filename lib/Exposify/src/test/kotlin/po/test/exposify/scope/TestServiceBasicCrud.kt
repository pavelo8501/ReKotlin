package po.test.exposify.scope

import org.junit.jupiter.api.assertAll
import po.auth.extensions.generatePassword
import po.exposify.extensions.WhereCondition
import po.exposify.scope.service.enums.TableCreateMode
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.TestClassItem
import po.test.exposify.setup.TestPages
import po.test.exposify.setup.dtos.TestPage
import po.test.exposify.setup.dtos.TestPageDTO
import po.test.exposify.setup.dtos.TestUser
import po.test.exposify.setup.dtos.TestUserDTO
import po.test.exposify.setup.pageModels
import po.test.exposify.setup.pageModelsWithSections
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TestServiceBasicCrud : DatabaseTest() {



}
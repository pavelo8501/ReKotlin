package po.test.misc.validators

import org.junit.jupiter.api.Test
import po.misc.exceptions.ManagedException
import po.misc.interfaces.Identifiable
import po.misc.interfaces.asIdentifiable
import po.misc.types.castOrThrow
import po.misc.validators.general.Validator
import po.misc.validators.general.models.CheckStatus
import po.misc.validators.general.validation

import kotlin.test.assertEquals


class TestValidator {

    @Test
    fun `Validator container`(){
        val identifiable: Identifiable = asIdentifiable("TestValidation", "TestValidator")
        val str = "aaaa"

        val validator = Validator()
        val report = validator.validate("Hopless casts", identifiable){

            validation("validation1", str){

            }

        }
        assertEquals(2, report.count())
        assertEquals(CheckStatus.FAILED,report.first().overallResult)
    }

}
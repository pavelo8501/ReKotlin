package po.test.misc.validators

import org.junit.jupiter.api.Test
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.Identifiable
import po.misc.context.asContext
import po.misc.validators.general.Validator
import po.misc.validators.general.models.CheckStatus
import po.misc.validators.general.validation

import kotlin.test.assertEquals


class TestValidator : CTX {

    override val identity: CTXIdentity<out CTX> = asContext()

    @Test
    fun `Validator container`(){

        val str = "aaaa"
        val validator = Validator()
        val report = validator.validate("Hopless casts", this){

            validation("validation1", str){

            }

        }
        assertEquals(2, report.count())
        assertEquals(CheckStatus.FAILED,report.first().overallResult)
    }

}
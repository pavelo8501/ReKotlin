package po.misc.validators.reports

import po.misc.data.helpers.orDefault
import po.misc.data.helpers.replaceIfNull
import po.misc.data.helpers.withIndent
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.printable.companion.Template
import po.misc.data.printable.companion.nextLine
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.data.templates.matchTemplate
import po.misc.data.templates.templateRule
import po.misc.types.token.TypeToken
import po.misc.validators.ValidationContainerBase
import po.misc.validators.models.CheckStatus


class ValidationRecord internal constructor(
    val validatingName: String,
    val recordName: String,
    val result: CheckStatus,
): PrintableBase<ValidationRecord>(this) {


    var message: String? = null
        private set

    override val self: ValidationRecord = this

    fun setMessage(message: String): ValidationRecord {
        this.message = message
        return this
    }


    companion object : PrintableCompanion<ValidationRecord>(TypeToken.create()) {

        val Main: Template<ValidationRecord> = createTemplate {
            nextLine {
                "Check:$recordName Status:${
                    result.name.matchTemplate(
                        templateRule(result.name.colorize(Colour.Green)) { result == CheckStatus.PASSED },
                        templateRule(result.name.colorize(Colour.Red)) { result == CheckStatus.FAILED },
                        templateRule(result.name.colorize(Colour.Yellow)) { result == CheckStatus.WARNING })
                }".withIndent(4, "-") + message.orDefault()
            }
        }

        fun success(container: ValidationContainerBase<*, *>, checkName: String): ValidationRecord {
            return ValidationRecord(container.validator.validatingCTXName, checkName, CheckStatus.PASSED)
        }

        fun fail(container: ValidationContainerBase<*, *>, checkName: String, message: String): ValidationRecord {
            return ValidationRecord(container.validator.validatingCTXName, checkName, CheckStatus.FAILED).setMessage(
                message
            )
        }

        fun fail(container: ValidationContainerBase<*, *>, checkName: String, th: Throwable): ValidationRecord {
            return ValidationRecord(
                container.validator.validatingCTXName,
                checkName,
                CheckStatus.FAILED
            ).setMessage(th.message.toString())
        }
    }
}
package po.misc.validators.reports

import po.misc.data.printable.Printable
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableGroup
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.printable.companion.Template
import po.misc.data.printable.companion.nextLine
import po.misc.data.printable.grouping.ArbitraryDataMap
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.data.templates.matchTemplate
import po.misc.data.templates.templateRule
import po.misc.types.token.TypeToken
import po.misc.validators.models.CheckStatus
import kotlin.reflect.KClass


class ValidationRecords(
    val report: ValidationReport
): PrintableGroup<ValidationReport, ValidationRecord>(report, ValidationReport.Header, ValidationRecord.Main){

   // override val ownClass: KClass<out Printable> = ValidationRecords::class

}


class ValidationReport(
    val validatingContextName: String,
    val validationName: String,
): PrintableBase<ValidationReport>(this) {

    override val self: ValidationReport = this
    val validationRecords: ValidationRecords = ValidationRecords(this)

    val overallResult: CheckStatus
        get() {
            return if (getRecords().all { it.result == CheckStatus.PASSED }) {
                CheckStatus.PASSED
            } else {
                CheckStatus.FAILED
            }
        }

    val hasFailures: Boolean get() = getRecords().any { it.result == CheckStatus.FAILED }

    init {
        validationRecords.setFooter(Footer)
    }

    fun addRecord(record: ValidationRecord): ValidationRecord {
        validationRecords.addRecord(record)
        return record
    }

    fun getRecords(): List<ValidationRecord> {
        return validationRecords.records
    }

    companion object : PrintableCompanion<ValidationReport>(TypeToken.create()) {

        val Header: Template<ValidationReport> = createTemplate {
            nextLine {
                "Validating $validatingContextName".colorize(Colour.Blue)
            }
            nextLine {
                validationName
            }
        }
        val Footer : Template<ValidationReport> = createTemplate {
            nextLine {
                """Overall Result: ${
                    overallResult.name.matchTemplate(
                        templateRule(overallResult.name.colorize(Colour.Green)) { overallResult == CheckStatus.PASSED },
                        templateRule(overallResult.name.colorize(Colour.Red)) { overallResult == CheckStatus.FAILED },
                        templateRule(overallResult.name.colorize(Colour.Yellow)) { overallResult == CheckStatus.WARNING }
                    )
                }
            """.trimMargin().colorize(Colour.Blue)
            }
        }
    }
}
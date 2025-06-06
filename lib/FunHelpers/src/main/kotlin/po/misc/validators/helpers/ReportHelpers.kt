package po.misc.validators.helpers

import po.misc.validators.models.CheckStatus
import po.misc.validators.reports.MappingReport

fun List<MappingReport>.finalCheckStatus(): CheckStatus{
    return if( this.map{ it }.any { report-> report.overallResult == CheckStatus.FAILED }){
        CheckStatus.FAILED
    }else{
        CheckStatus.PASSED
    }
}
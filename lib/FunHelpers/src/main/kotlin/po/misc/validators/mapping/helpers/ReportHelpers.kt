package po.misc.validators.mapping.helpers

import po.misc.validators.mapping.models.CheckStatus
import po.misc.validators.mapping.reports.MappingReport

fun List<MappingReport>.finalCheckStatus(): CheckStatus{
    return if( this.map{ it }.any { report-> report.overallResult == CheckStatus.FAILED }){
        CheckStatus.FAILED
    }else{
        CheckStatus.PASSED
    }
}
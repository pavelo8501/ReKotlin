package po.misc.validators.general.reports

import po.misc.validators.general.models.CheckStatus


fun List<ValidationReport>.finalCheckStatus(): CheckStatus{
    return if( this.map{ it }.any { report-> report.overallResult == CheckStatus.FAILED }){
        CheckStatus.FAILED
    }else{
        CheckStatus.PASSED
    }
}
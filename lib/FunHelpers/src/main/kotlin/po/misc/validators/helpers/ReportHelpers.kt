package po.misc.validators.helpers

import po.misc.validators.models.CheckStatus
import po.misc.validators.models.ClassMappingReport

fun List<ClassMappingReport>.result(): CheckStatus{
    return if( this.map{ it }.any { report-> report.overallResult == CheckStatus.FAILED }){
        CheckStatus.FAILED
    }else{
        CheckStatus.PASSED
    }
}
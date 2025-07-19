package po.lognotify.common.result

import po.misc.reflection.classes.ClassInfo

class PreliminaryResult<R: Any?>(
    val classInfo: ClassInfo<R>
) {

    private var acceptableResult:R?  = null
    private var resultProvided: Boolean = false

    var resultAcceptsNulls: Boolean = classInfo.acceptsNull

    fun provideAcceptableResult(value:R?):PreliminaryResult<R>{
        resultProvided = true
        acceptableResult = value
        return  this
    }


    fun getAcceptableResult():R{
        return acceptableResult as R
    }

}
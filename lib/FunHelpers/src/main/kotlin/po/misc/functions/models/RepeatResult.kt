package po.misc.functions.models



class RepeatResult<R>(
    var result:R? = null
){
    val exceptions: MutableList<Throwable> = mutableListOf()
    val hasResult: Boolean get() = result != null

    var resultResolved: Boolean = false
    var exceptionResolved: Boolean = false

    val lastException: Throwable? get(){
        return exceptions.lastOrNull()
    }

    fun provideResult(result:R):RepeatResult<R>{
        this.result = result
        resultResolved =true
        return this
    }

    fun addException(th: Throwable):RepeatResult<R>{
        exceptions.add(th)
        exceptionResolved = true
        return this
    }

    override fun toString(): String {
        return """
           RepeatResult[
             hasResult = $hasResult;
             exceptionsCount = ${exceptions.size};  
           ]"""
    }
}
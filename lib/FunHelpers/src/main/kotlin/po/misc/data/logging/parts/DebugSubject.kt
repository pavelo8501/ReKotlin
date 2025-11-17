package po.misc.data.logging.parts

import po.misc.context.tracable.TraceableContext
import po.misc.data.logging.log_subject.DebugSubject
import po.misc.data.logging.log_subject.SubjectBase
import po.misc.data.logging.log_subject.updateSubject
import po.misc.debugging.ClassResolver


object DebugMethod : SubjectBase(DebugSubject.Debug), DebugSubject {

    fun methodName(name: String, text: String? = null):DebugMethod{
        if(text != null){
            updateSubject("Method name : $name", text)
        }else{

            changeSubject(name)
        }
        return this
    }
}

object SubjectDebugState : SubjectBase(DebugSubject.Debug), DebugSubject {
    fun provideState(context: TraceableContext, text: String): DebugSubject {
        val info = ClassResolver.classInfo(context)
        changeSubject("State : $info", text)
        return this
    }
}
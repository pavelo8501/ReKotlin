package po.misc.data.logging

import po.misc.data.styles.Colour
import po.misc.data.styles.colorize




enum class NotificationTopic(val value: Int): Comparable<NotificationTopic>{

    Debug(0),
    Info(1),
    Warning(2),
    Exception(3);

    companion object{

        fun formatterName(topic: NotificationTopic): String{
           return when(topic){
                Warning-> topic.name.colorize(Colour.Yellow)
                Exception -> topic.name.colorize(Colour.Red)
                else ->  topic.name
            }
        }
    }
}



sealed interface NotificationTopic2{
    val value: Int
    val code: Int
    fun changeCode(newCode: Int)
    fun copy(newCode: Int = code):NotificationTopic2


    object Debug: DebugClass(value = 0, code = 0)

}


open class DebugClass(
    override val value: Int,
    override var code: Int
): NotificationTopic2{

    override fun changeCode(newCode: Int){
        code = newCode
    }

    override fun copy(newCode: Int):DebugClass{
        return DebugClass(value,  newCode)
    }

}










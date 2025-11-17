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
package po.misc.data.logging


enum class Verbosity(val minTopic: Topic){
    Debug(Topic.Debug),
    Info(Topic.Info),
    Warnings(Topic.Warning);

    fun minTopicReached(topic: Topic):Boolean{
        return topic >= this.minTopic
    }
}
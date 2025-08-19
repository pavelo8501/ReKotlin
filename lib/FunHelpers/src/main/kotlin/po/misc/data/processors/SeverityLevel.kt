package po.misc.data.processors


enum class SeverityLevel (val level: Int) {
    INFO(1), // Messages
    WARNING(2), // Yet non-critical exception occurrences, other code anomalies
    EXCEPTION(2), //Final exception throwing point, with thread being terminated
    DEBUG(4);

    companion object {
        val emojis : Map<Int, String> = mapOf(1 to "ℹ️",  2  to "⚠️", 3  to  "❌", 4 to "ℹ️")
        fun fromValue(severityLevelId: Int): SeverityLevel? {
            entries.firstOrNull { it.level == severityLevelId }?.let {
                return it
            }
            return INFO
        }

        fun emojiByValue(value : SeverityLevel):String{
            return emojis[value.level]?:return ""
        }
    }
}
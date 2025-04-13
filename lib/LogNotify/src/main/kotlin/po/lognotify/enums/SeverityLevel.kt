package po.lognotify.enums

enum class SeverityLevel (val severityLevelId: Int) {
    INFO(1),
    WARNING(2),
    EXCEPTION(3);

    companion object {

         val emojis : Map<Int, String> = mapOf(1 to "ℹ️",  2  to "⚠️", 3  to  "❌", 4 to "ℹ️")

        fun fromValue(severityLevelId: Int): SeverityLevel? {
            entries.firstOrNull { it.severityLevelId == severityLevelId }?.let {
                return it
            }
            return INFO
        }

        fun emojiByValue(value : SeverityLevel):String{
            return emojis[value.severityLevelId]?:return ""
        }
    }
}
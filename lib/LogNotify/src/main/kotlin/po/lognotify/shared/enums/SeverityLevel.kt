package po.lognotify.shared.enums

enum class SeverityLevel (val level: Int) {
    INFO(1),
    TASK(2),
    WARNING(3),
    EXCEPTION(4);

    companion object {
        fun fromValue(level: Int): SeverityLevel? {
            SeverityLevel.entries.firstOrNull { it.level == level }?.let {
                return it
            }
            return INFO
        }
    }
}
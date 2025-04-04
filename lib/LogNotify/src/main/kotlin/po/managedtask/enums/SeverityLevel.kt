package po.managedtask.enums

enum class SeverityLevel (val level: Int) {
    INFO(1),
    WARNING(3),
    EXCEPTION(4);

    companion object {
        fun fromValue(level: Int): SeverityLevel? {
            entries.firstOrNull { it.level == level }?.let {
                return it
            }
            return INFO
        }
    }
}
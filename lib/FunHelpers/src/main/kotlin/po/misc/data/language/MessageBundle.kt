package po.misc.data.language

import com.typesafe.config.ConfigFactory
import java.util.Properties


object MessageBundle {
    private val config = ConfigFactory.load("messages.conf")

    fun get(path: String, vararg args: Any): String {
        val template = try {
            config.getString(path)
        } catch (e: Exception) {
            "??$path??"
        }
        return args.foldIndexed(template) { i, acc, arg ->
            acc.replace("{$i}", arg.toString())
        }
    }
}
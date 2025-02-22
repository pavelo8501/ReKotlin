package po.restwraptor.interfaces

interface StringHelper {

    fun toUrl(vararg pathParts: String): String

    companion object : StringHelper {
        override fun toUrl(vararg pathParts: String): String =
            pathParts
                .asSequence()
                .map { it.trim().trim('/') }
                .filter { it.isNotEmpty() }
                .joinToString("/")
    }

}
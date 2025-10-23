package po.misc.io

import kotlinx.serialization.Serializable


@Serializable
enum class FileType{
    Text,
    Image,
    Json,
    Other
}
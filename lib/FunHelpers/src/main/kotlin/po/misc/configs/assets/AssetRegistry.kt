package po.misc.configs.assets

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import po.misc.context.Component
import po.misc.data.logging.Verbosity
import po.misc.io.toSafePathName


@Serializable
class AssetRegistry(
    val category: String
): Component {

    @Transient
    internal val categoryAsFileName = category.toSafePathName()
    @Transient
    internal var updated: ((AssetRegistry)-> Unit)? = null

    constructor(category: Enum<*>):this(category.name.toSafePathName())

    val assets: MutableMap<String, Asset> = mutableMapOf()

    @Transient
    override var verbosity: Verbosity = Verbosity.Info
    @Transient
    override val componentName: String = "AssetRegistry[$category]"

    private fun assetUpdated(asset: Asset){
        info("Update", "Updating ${asset.name}")
        updated?.invoke(this)
    }

    fun add(asset: Asset): Asset {
        asset.updated = ::assetUpdated
        assets[asset.name] = asset
        updated?.invoke(this) ?:run {
            warn("Update", "$componentName will not be saved to FS. Not initialized")
        }
        return asset
    }

    fun buildOrGet(builder: ()-> RegistryAsset): RegistryAsset?{
        val asset = builder()
        return if(updated != null){
            val normalized = normalizeAssetName(asset.name)
            assets[normalized] ?:run {
                val newAsset = Asset(normalized, asset.filePath, asset.fileID, asset.description)
                add(newAsset)
            }
        }else{
            null
        }
    }

    operator fun get(key: String): Asset? = assets[key]

    override fun equals(other: Any?): Boolean {
       return when(other){
            is String ->{
                category == other
            }
            is Enum<*> ->{
                category == other.name.lowercase()
            }
            is AssetRegistry->{
                other.category == category
            }
            else -> false
        }
    }

    companion object{
        fun normalizeAssetName(name: String): String{
            return name.trim().lowercase().trim(' ')
        }
    }

    override fun hashCode(): Int {
        return category.hashCode()
    }

}
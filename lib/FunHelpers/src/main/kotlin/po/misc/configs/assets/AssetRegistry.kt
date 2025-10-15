package po.misc.configs.assets

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import po.misc.context.Component
import po.misc.data.logging.Verbosity
import po.misc.io.buildRelativePath
import po.misc.io.readFile
import po.misc.io.readToString
import po.misc.io.toSafePathName
import po.misc.io.writeToFile
import java.io.FileNotFoundException

@Serializable
data class AssetRegistryData(
    val category: String
){
    val assets: MutableMap<String, Asset> = mutableMapOf()


}

class AssetRegistry2(
    val category: String,
    val basePath: String,
    val json: Json
): Component {

    val registryPath: String = buildRelativePath(basePath, category) + ".json"

    var registry: AssetRegistryData? = null
        private set



    override var verbosity: Verbosity = Verbosity.Info
    override val componentName: String = "AssetRegistry[$category]"

    constructor(category: Enum<*>, relativePath: String, json: Json):this(category.name, relativePath, json)

    private fun getOrBuild():AssetRegistryData{
       return  registry?:run {
            AssetRegistryData(category)
        }
    }

    private fun tryLoadRegistry(): AssetRegistryData? {
        val registryPayload = registry
        return if(registryPayload == null){
            val jsonString =  readFile(registryPath).readBytes().readToString()
            val loaded = json.decodeFromString<AssetRegistryData>(jsonString)
            info("Laded", "Registry $category loaded")
            loaded
        }else{
            info("Cached", "Registry $category loaded")
            registryPayload
        }
    }

    private fun loadRegistry(): AssetRegistryData {
       val registryPayload = registry
       return if(registryPayload == null){
            val jsonString =  readFile(registryPath).readBytes().readToString()
            val loaded = json.decodeFromString<AssetRegistryData>(jsonString)
           info("Laded", "Registry $category loaded")
           loaded
        }else{
            info("Cached", "Registry $category loaded")
            registryPayload
        }
    }

    private fun writeCreating():AssetRegistryData{
        val registry = getOrBuild()
        json.encodeToString<AssetRegistryData>(registry).writeToFile(registryPath)
        return registry
    }

    fun loadOrCreate(): AssetRegistry2{
        registry = try {
            loadRegistry()
        }catch (th: Throwable){
            when(th){
                is FileNotFoundException  ->{
                    info("Creating", "Registry not found creating")
                    writeCreating()
                }
                is SerializationException if th.message?.contains("Encountered an unknown key")?:false ->{
                    warn("Creating", "Registry had broken keys recreating")
                    writeCreating()
                }
                else -> {
                    warn(th)
                    throw th
                }
            }
        }
        return this
    }
}


@Serializable
class AssetRegistry(
    val category: String
): Component {

    @Transient
    internal val categoryAsFileName = category.toSafePathName()
    @Transient
    internal var updated: ((AssetRegistry)-> Unit)? = null
    @Transient
    internal var erased: ((AssetRegistry)-> Boolean)? = null

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

    fun purge(): Boolean{
       val tempAssets  = assets.values
       return erased?.let {
            assets.clear()
            val lambdaResult = it.invoke(this)
            if(lambdaResult){
                return true
            }else{
                tempAssets.forEach {asset->
                    assets[asset.name] = asset
                }
                false
            }
        }?:run {
            warn("purge", "Can not be purged. Not initialized")
            false
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
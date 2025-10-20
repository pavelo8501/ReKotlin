package po.misc.configs.assets

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import po.misc.context.component.Component
import po.misc.context.component.ComponentID
import po.misc.context.component.componentID
import po.misc.context.component.managedException
import po.misc.data.logging.Verbosity
import po.misc.functions.Throwing
import po.misc.io.LocalFile
import po.misc.io.SourcedFile
import po.misc.io.WriteOptions
import po.misc.io.buildRelativePath
import po.misc.io.deleteFile
import po.misc.io.readSourced
import po.misc.io.writeSourced
import java.io.FileNotFoundException


@Serializable
data class RegistryPayload(
    val category: String,
){
    val assets: MutableMap<String, Asset> = mutableMapOf()
}

class AssetRegistry(
    val category: String,
    override var basePath: String,
    val json: Json
): Component, ConfigHolder {

    private var assetStatusChanged: (AssetRegistry.(Asset)-> Unit)? = null

    val registryPath: String = buildRelativePath(basePath, category) + ".json"
    var registrySource: SourcedFile<RegistryPayload>? = null
        private set

    override val componentID: ComponentID = componentID("AssetRegistry[$category]")

    override var verbosity: Verbosity = Verbosity.Info


    val assets: Map<String,  Asset> get() =   registrySource?.source?.assets?:emptyMap()
    val assetList: List<Asset> get() =  assets.values.toList()
    val size: Int get() = registrySource?.source?.assets?.size?:0

    val updatePending: Int get() = assetList.count{ it.updatePending }

    internal var assetManager: AssetManager? = null

    constructor(category: Enum<*>, relativePath: String, json: Json):this(category.name, relativePath, json)
    constructor(manager: AssetManager, category: String,):this(category, manager.basePath, manager.jsonEncoder){
        assetManager = manager
    }
    override var config: AssetManager.ConfigData = AssetManager.ConfigData(basePath = registryPath)

    private fun getOrBuild():RegistryPayload {
        return registrySource?.source ?: run {
            RegistryPayload(category)
        }

    }

    private fun load(): SourcedFile<RegistryPayload>{
        val registryFile = registrySource
        return if(registryFile == null){
            val loaded = readSourced(registryPath, Charsets.UTF_8){
                json.decodeFromString<RegistryPayload>(it)
            }
            info("Laded", "Registry $category loaded")
            loaded
        }else{
            info("Cached", "Registry $category loaded")
            registryFile
        }
    }

    private fun writeCreating():SourcedFile<RegistryPayload>{
        val registry = getOrBuild()
        return registry.writeSourced(registryPath, Charsets.UTF_8){
            json.encodeToString<RegistryPayload>(registry)
        }
    }

    private fun requireSourced(operation: String):SourcedFile<RegistryPayload>{
        val registry = registrySource
        if(registry == null){
            val errorMsg: String = "Require Registry resulted in failure with message. " +
                    "Trying to $operation to uninitialized registry $category"
            warn(operation, errorMsg)
            throw IllegalStateException(errorMsg)
        }else{
            return registry
        }
    }

    internal fun clearSource(){
        registrySource = null
    }

    fun onAssetStatusChanged(callback: AssetRegistry.(Asset)-> Unit){
        assetStatusChanged = callback
    }

    fun addAsset(
        metaData: LocalFile,
        name: String,
    ): Asset?{
        val sourcedFile = registrySource
        val assetName = normalizeAssetName(name)
        if(sourcedFile == null){
            info("Initialization", "Registry $category not initialized. Unable to add asset $assetName")
            return null
        }else{
            val existentAsset =  sourcedFile.source.assets[assetName]
            return if(existentAsset!=null){
                info("Initialization", "Registry $category contains asset $assetName")
                existentAsset.initialize(this)
                assetStatusChanged?.let { registryLambda ->
                    existentAsset.statusChanged{
                        registryLambda.invoke(this, it)
                    }
                }
                info("Initialization", "Asset ${existentAsset.name} reinitialized")
                existentAsset
            }else{
                val newAsset = Asset(this, metaData, name = assetName)
                sourcedFile.source.assets[assetName] = newAsset
                info("Initialization", "Asset  $assetName applied to Registry $category. Total assets count ${ sourcedFile.source.assets.size}")
                assetStatusChanged?.let { registryLambda ->
                    newAsset.statusChanged{
                        registryLambda.invoke(this, it)
                    }
                }
                newAsset
            }
        }
    }

    fun addAsset(
        metaData: LocalFile,
        assetName: NamedAsset,
    ): Asset? = addAsset(metaData, assetName.name)

    fun addAsset(
        throwing: Throwing,
        metaData: LocalFile,
        name: String
    ): Asset{
        val sourcedFile = requireSourced("Add asset")
        val assetName = normalizeAssetName(name)
        val existentAsset =  sourcedFile.source.assets[assetName]
       return if(existentAsset != null){
            info("Initialization", "Registry $category contains asset $assetName")
            existentAsset.initialize(this)
            assetStatusChanged?.let { registryLambda ->
                existentAsset.statusChanged{
                    registryLambda.invoke(this, it)
                }
            }
            info("Initialization", "Asset ${existentAsset.name} initialized")
            existentAsset
        }else{
           val newAsset = Asset(this, metaData, name = assetName)
           sourcedFile.source.assets[assetName] = newAsset
           info("Initialization", "Asset  $assetName applied to Registry $category. Total assets count ${ sourcedFile.source.assets.size}")
            assetStatusChanged?.let { registryLambda ->
                newAsset.statusChanged{
                    registryLambda.invoke(this, it)
                }
            }
            newAsset
        }
    }
    fun addAsset(
        throwing: Throwing,
        metaData: LocalFile,
        assetName: NamedAsset,
    ): Asset = addAsset(throwing, metaData, name = assetName.name)

    fun addAsset(asset:Asset): Asset {
        val sourcedFile = requireSourced("Add asset")
        val existentAsset = sourcedFile.source.assets[asset.name]
        return if (existentAsset != null) {
            info("Initialization", "Registry $category contains asset ${asset.name}")
            existentAsset.initialize(this)
            assetStatusChanged?.let { registryLambda ->
                existentAsset.statusChanged {
                    registryLambda.invoke(this, it)
                }
            }
            info("Initialization", "Asset ${existentAsset.name} initialized")
            existentAsset
        } else {
            sourcedFile.source.assets[asset.name] = asset
            info(
                "Initialization",
                "Asset  $asset.name applied to Registry $category. Total assets count ${sourcedFile.source.assets.size}"
            )
            assetStatusChanged?.let { registryLambda ->
                asset.statusChanged {
                    registryLambda.invoke(this, it)
                }
            }
            asset
        }
    }

    fun get(name: String): Asset?{
       return assets[name]
    }
    fun get(assetName:  NamedAsset): Asset?{
        return assets[assetName.name]
    }

    fun get(throwing: Throwing, name: String): Asset{
        return assets[name].getOrThrow {
            managedException("Asset $name not found")
        }
    }
    fun get(throwing: Throwing, assetName: NamedAsset): Asset = get(throwing, assetName.name)

    fun loadAndBuild(builder: AssetRegistry.()-> Unit){
         loadOrCreate()
         builder()
    }

    fun loadOrCreate(): AssetRegistry{
        registrySource = try {
            load()
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
                    warn("Creating" ,th)
                    throw th
                }
            }
        }
        return this
    }

    fun commitChanges(): Boolean {
        if (updatePending == 0) { return false }
        return registrySource?.let { sourcedFile ->
            for(asset in assetList){
                when(asset.state){
                    Asset.State.Updated ->{
                        asset.state = Asset.State.InSync
                    }
                    Asset.State.MarkedDelete->{
                        sourcedFile.source.assets.remove(asset.name)
                    }
                    Asset.State.MarkedDeleteWithFile->{
                        deleteFile(asset.filePath)
                        sourcedFile.source.assets.remove(asset.name)
                    }
                    else -> {}
                }
            }
            val bytes = json.encodeToString<RegistryPayload>(sourcedFile.source).toByteArray()
            sourcedFile.updateSource(sourcedFile.source, bytes)
        } ?: false
    }

    @Deprecated("Change to commitChanges")
    fun commit(): Boolean = commitChanges()

    fun deleteAsset(name: String, includingFile: Boolean): Boolean{
        val assetMap = registrySource?.source?.assets ?: return false
        return assetMap[name]?.let {asset->
            if(includingFile) {
                asset.state = Asset.State.MarkedDeleteWithFile
            }else {
                asset.state = Asset.State.MarkedDelete
            }
            true
        }?:false
    }

    fun deleteAsset(asset: Asset, includingFile: Boolean): Boolean
        = deleteAsset(asset.name, includingFile)

    fun purge(){
        val source = RegistryPayload(category)
        registrySource =  source.writeSourced(registryPath, Charsets.UTF_8, WriteOptions(overwriteExistent = true)){
            json.encodeToString(source)
        }
    }

    companion object{
        fun normalizeAssetName(name: String): String{
            return name.trim()
        }
        fun normalizeAssetName(assetName: NamedAsset): String{
            return assetName.name.trim()
        }
    }
}

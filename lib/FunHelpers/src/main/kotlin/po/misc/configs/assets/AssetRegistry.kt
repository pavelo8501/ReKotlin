package po.misc.configs.assets

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import po.misc.context.Component
import po.misc.data.logging.Verbosity
import po.misc.functions.Throwing
import po.misc.io.LocalFile
import po.misc.io.SourcedFile
import po.misc.io.WriteOptions
import po.misc.io.buildRelativePath
import po.misc.io.deleteFile
import po.misc.io.readSourced
import po.misc.io.stripFileExtension
import po.misc.io.writeSourced
import java.io.FileNotFoundException
import kotlin.math.log


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

    override var verbosity: Verbosity = Verbosity.Info
    override val componentName: String = "AssetRegistry[$category]"

    val size: Int get() = registrySource?.source?.assets?.size?:0
    val updatePending: Int get() = assets.count{ it.updatePending }

    val assets: List<Asset> get() =   registrySource?.source?.assets?.values?.toList()?:emptyList()

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

    internal fun clearSource(){
        registrySource = null
    }

    fun onAssetStatusChanged(callback: AssetRegistry.(Asset)-> Unit){
        assetStatusChanged = callback
    }

    fun addAsset(metaData: LocalFile, fileID: String? = null, name: String? = null): Asset?{


        val sourcedFile = registrySource
        val assetName = name?:metaData.fileName.stripFileExtension()
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
                val newAsset = Asset(this, metaData, newFileID = fileID, name =  name)
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
    fun addAsset(throwing: Throwing, metaData: LocalFile, fileID: String? = null, name: String? = null): Asset{
        val sourcedFile :  SourcedFile<RegistryPayload> = registrySource ?: throw IllegalStateException("Trying to add asset to uninitialized registry $category")
        val assetName = name?:metaData.fileName.stripFileExtension()
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
           val newAsset = Asset(this, metaData, newFileID = fileID, name =  name)
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
                    warn(th)
                    throw th
                }
            }
        }
        return this
    }
    fun commit(): Boolean {
        if (updatePending == 0) { return false }
        return registrySource?.let { sourcedFile ->
            for(asset in assets){
                when(asset.state){
                    Asset.State.Updated ->{
                        asset.state =  Asset.State.InSync
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

    fun deleteAsset(name: String, includingFile: Boolean): Boolean{

        val assetMap = registrySource?.source?.assets ?: return false
        return assetMap[name]?.let {
            if(includingFile){
                it.state = Asset.State.MarkedDeleteWithFile
            }else{
                it.state = Asset.State.MarkedDelete
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
}

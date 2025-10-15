package po.misc.configs.assets

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import po.misc.context.Component
import po.misc.data.logging.Verbosity
import po.misc.functions.Throwing
import po.misc.io.FileMetaData
import po.misc.io.WriteOptions
import po.misc.io.readFile
import po.misc.io.readToString
import po.misc.io.toSafePathName
import po.misc.io.writeFileContent
import po.misc.io.writeToFile


/**
 * @param basePath relative path to the root folder
 */
class AssetManager(
    basePath: String,
    val jsonEncoder: Json,
): Component {

    data class ConfigData(
        override var basePath: String,
        override var recreateKeysChanged: Boolean = true,
        override var verbosity: Verbosity = Verbosity.Info
    ): Config{

        fun setBasePath(relativePath: String):ConfigData{
            basePath = relativePath
            return this
        }

    }

    var config: ConfigData = ConfigData(basePath = basePath)

    constructor(basePath: String, jsonEncoder: Json, configBuilder: AssetsConfigurator.()-> Unit):this("", jsonEncoder){
       val configurator = AssetsConfigurator()
        configurator.config.basePath = basePath
        configBuilder.invoke( configurator)
        config = configurator.config
    }

    constructor(assetsConfig: AssetsKeyConfig, jsonEncoder: Json, configBuilder: AssetsConfigurator.()-> Unit):this("", jsonEncoder){
        val configurator = AssetsConfigurator()
        configurator.fromAssetsConfig(assetsConfig)
        configBuilder.invoke(configurator)
        config = configurator.config
    }


    override val verbosity: Verbosity get() =  config.verbosity

    override val componentName: String = "AssetManager"
    private val initSubject: String = "$componentName Initialization"
    private val operationsSubject: String = "$componentName Update"
    private val registries = mutableMapOf<String, AssetRegistry>()

    internal fun createPath(category: String): String{
       return "${config.basePath}/${category.toSafePathName()}.json"
    }

    /**
     * If loaded by category no callbacks assigned
     */
    private fun loadRegistry(category: String, noCache: Boolean): AssetRegistry {
        val normalized = category.toSafePathName()
        val existent = registries[normalized]

        if(noCache && existent != null) {
            registries.remove(normalized)
        }
        if(!noCache && existent != null) {
            return  existent
        }
        val pathToFile =  createPath(normalized)
        val jsonString =  readFile(pathToFile).readBytes().readToString()
        val decoded =   jsonEncoder.decodeFromString<AssetRegistry>(jsonString)
        info(initSubject, "${decoded.category} loaded")
        return decoded
    }

    private fun loadRegistry(registry: AssetRegistry, noCache: Boolean): AssetRegistry {
        val name =  registry.category.toSafePathName()
        val loaded = try {
                loadRegistry(name, noCache)
            } catch (th: NullPointerException) {
                jsonEncoder.encodeToString(registry)
                    .writeFileContent(name, WriteOptions(overwriteExistent = false, createSubfolders = true))
                 registry
            } catch (serializationException: SerializationException) {
                val encounteredMessage = serializationException.message ?: ""
                if (encounteredMessage.contains("Encountered an unknown key")) {
                    val pathToFile = createPath(name)
                    warn(operationsSubject, "Recreating registry")
                    val encoded = jsonEncoder.encodeToString(registry)
                    encoded.toByteArray()
                        .writeToFile(pathToFile, WriteOptions(overwriteExistent = true, createSubfolders = true))
                    registry
                } else {
                    warn(serializationException)
                    throw serializationException
                }
        } catch (th: Throwable) {
            warn(th)
            throw th
        }

        loaded.assets.forEach {
            registry.assets[it.key] =it.value
        }
        registry.updated = ::registryUpdated
        registry.erased = ::tryWriteRegistry
        return  registry
    }

    internal fun tryLoadRegistry(category: String):AssetRegistry?{
        return try {
            return  loadRegistry(category, noCache = false)
        }catch (th: Throwable){
            warn(th)
            null
        }
    }

    private fun tryLoadRegistry(registry: AssetRegistry, noCache: Boolean): AssetRegistry? {
        return try {
            loadRegistry(registry, noCache)
        }catch (th: Throwable){
            warn(th)
            null
        }
    }

    private fun tryWriteRegistry(registry: AssetRegistry): Boolean{
        return try {
            val pathToFile = createPath(registry.category)
            jsonEncoder.encodeToString(registry)
                .writeFileContent(pathToFile, WriteOptions(overwriteExistent = true, createSubfolders = false))
            true
        }catch (th: Throwable){
            warn(th)
            false
        }
    }

    private fun registryUpdated(registry: AssetRegistry): Boolean{
        try {
            val pathToFile = createPath(registry.category)
            val jsonString = jsonEncoder.encodeToString(registry)
            jsonString.writeFileContent(pathToFile, WriteOptions(overwriteExistent = true, createSubfolders = true))
            info(operationsSubject, "Registry saved")
            return true
        }catch (th: Throwable){
            warn(th)
            return false
        }
    }

    fun applyRegistry(registry: AssetRegistry):AssetRegistry?{
       val loaded = tryLoadRegistry(registry, true)
       if(loaded != null){
           registries[loaded.category] = loaded
       }
       return loaded
    }

    fun applyRegistries(registry: List<AssetRegistry>):List<AssetRegistry>{
        return registry.mapNotNull { applyRegistry(it) }
    }

    fun applyRegistry(registry: AssetRegistry, throwing: Throwing):AssetRegistry{
        val loaded =  loadRegistry(registry, true)
        registries[loaded.category] = loaded
        return loaded
    }

    fun applyRegistry(registry: AssetRegistry, builder: AssetRegistry.()-> Unit):AssetRegistry?{
       val registry = applyRegistry(registry)
       if(registry != null){
           registry.builder()
           return  registry
       }else{
           return null
       }
    }

    fun applyRegistry(registry: AssetRegistry, throwing: Throwing, builder: AssetRegistry.()-> Unit):AssetRegistry{
        val registry = applyRegistry(registry, throwing)
        registry.builder()
        return registry
    }

    fun createOrLoad(name: String,  category: String, fileMeta: FileMetaData): Asset?{
        val normalizedCategory = category.toSafePathName()
        val registry = registries[normalizedCategory]
        return if(registry != null){
            registry[name] ?:run {
                val asset = Asset(name, fileMeta.relativePath)
                registry.add(asset)
            }
        }else{
            null
        }
    }

    fun createOrLoad(name: String, category: Enum<*>, fileMeta: FileMetaData): Asset?
        = createOrLoad(name, category.name, fileMeta)

    fun purge(registry: AssetRegistry):AssetRegistry?{
        val registry =  tryLoadRegistry(registry, false)
        if(registry != null){
            registry.purge()
            registries[registry.category] = registry
            return registry
        }
        return registry
    }

    companion object{

    }

}
package po.misc.configs.assets

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import po.misc.context.Component
import po.misc.data.logging.Verbosity
import po.misc.functions.Throwing
import po.misc.io.FileMetaData
import po.misc.io.WriteOptions
import po.misc.io.fileExists
import po.misc.io.readToString
import po.misc.io.writeFileContent


/**
 * @param basePath relative path to the root folder
 */
class AssetManager(
    val basePath: String,
    val jsonEncoder: Json,
    val config: Config = Config()
): Component {

    data class Config(
        val recreateKeysChanged: Boolean = true,
        val verbosity: Verbosity = Verbosity.Info
    )

    override val verbosity: Verbosity get() =  config.verbosity

    override val componentName: String = "AssetManager"
    private val initSubject: String = "$componentName Initialization"
    private val operationsSubject: String = "$componentName Update"
    private val registries = mutableMapOf<String, AssetRegistry>()

    private fun normalizeRegistryName(category: String): String{
       return category.trim().lowercase().trim(' ')
    }

    private fun tryLoadRegistry(registry: AssetRegistry): AssetRegistry? {
        return try {
            val pathToFile = "${basePath}/${registry.categoryAsFileName}.json"
            val recreated = fileExists(pathToFile)?.let {
                val bytes = it.readFile()
                val jsonString = bytes.readToString()
                jsonEncoder.decodeFromString<AssetRegistry>(jsonString)
            } ?: run {
                jsonEncoder.encodeToString(registry)
                    .writeFileContent(pathToFile, WriteOptions(overwriteExistent = false, createSubfolders = true))
                registry
            }
            info(initSubject, "${recreated.category} loaded")
            recreated
        }catch (serializationException: SerializationException){
          return  if(serializationException.message?.contains("Encountered an unknown key")?:false){
                val pathToFile = "${basePath}/${registry.categoryAsFileName}.json"
               warn(operationsSubject, "Recreating registry")
               val fileWriteResult = jsonEncoder.encodeToString(registry)
                    .writeFileContent(pathToFile, WriteOptions(overwriteExistent = true, createSubfolders = true))
               if(fileWriteResult){
                   registry
               }else{
                   null
               }
            }else{
                null
            }
        }catch (th: Throwable){
            warn(th)
            null
        }
    }

    private fun loadRegistry(registry: AssetRegistry): AssetRegistry {
        return try {
            val pathToFile = "${basePath}/${registry.categoryAsFileName}.json"
            val recreated = fileExists(pathToFile)?.let {
                val bytes = it.readFile()
                val jsonString = bytes.readToString()
                jsonEncoder.decodeFromString<AssetRegistry>(jsonString)
            } ?: run {
                jsonEncoder.encodeToString(registry)
                    .writeFileContent(pathToFile, WriteOptions(overwriteExistent = false, createSubfolders = true))
                registry
            }
            info(initSubject, "${recreated.category} loaded")
            recreated
        }catch (th: Throwable){
            warn(th)
            throw th
        }
    }

    private fun registryUpdated(registry: AssetRegistry): Boolean{
        try {
            val pathToFile = "${basePath}/${registry.categoryAsFileName}.json"
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
       val loaded = tryLoadRegistry(registry)
       if(loaded != null){
           registries[loaded.category] = loaded
           loaded.updated = ::registryUpdated
       }
       return loaded
    }

    fun applyRegistries(registry: List<AssetRegistry>):List<AssetRegistry>{
        return registry.mapNotNull { applyRegistry(it) }
    }

    fun applyRegistry(registry: AssetRegistry, throwing: Throwing):AssetRegistry{
        val loaded =  loadRegistry(registry)
        registries[loaded.category] = loaded
        loaded.updated = ::registryUpdated
        return loaded
    }

    fun createOrLoad(name: String,  category: String, fileMeta: FileMetaData): Asset?{
        val normalizedCategory = normalizeRegistryName(category)
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


}
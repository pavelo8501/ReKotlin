package po.misc.configs.assets

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import po.misc.context.Component
import po.misc.data.logging.Verbosity
import po.misc.io.WriteOptions
import po.misc.io.fileExists
import po.misc.io.readToString
import po.misc.io.writeFileContent
import java.io.File


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
    private val registries = mutableListOf<AssetRegistry>()

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
           registries.add(loaded)
           loaded.updated = ::registryUpdated
       }
       return loaded
    }




//    constructor(path: String, jsonEncoder: Json, predefinedAssets: List<Pair<String,AssetInfo>>? = null):this(jsonEncoder){
//        registryPath = path
//        registry = loadRegistry(path)
//        val toBeUpdates = mutableListOf<Pair<String,AssetInfo>>()
//        predefinedAssets?.forEach { pair->
//            registry.assets[pair.first]?:run {
//                toBeUpdates.add(pair)
//            }
//        }
//        toBeUpdates.forEach {
//            update(it.first, it.second)
//        }
//    }
//
//


//    private fun loadRegistry(): AssetRegistry =
//        if (file.exists()) jsonEncoder.decodeFromString(file.readText())
//        else AssetRegistry()
//
//    private fun loadRegistry(path: String):AssetRegistry{
//        val bytes =  readFileContent(path){
//            onError {
//                it.output()
//            }
//        }
//        return  jsonEncoder.decodeFromString<AssetRegistry>(bytes.readToString())
//    }

//    fun provideConfig(path: String, predefinedAssets: List<Pair<String,AssetInfo>>? = null): List<AssetInfo>{
//        registryPath = path
//       // loadRegistry(path)
//        predefinedAssets?.let {list->
//            list.forEach {pair->
//                update(pair.first, pair.second)
//            }
//        }
//        return registry.assets.map { it.value }
//    }
//
//    fun allAssets(): List<AssetInfo>{
//        return registry.assets.map { it.value }
//    }
//
//    fun get(key: String): AssetInfo? = registry[key]
//
//    fun getAsset(key: Enum<*>): AssetInfo?{
//        return get(key.name)
//    }
//
//    fun update(key: String, info: AssetInfo) {
//        registry[key] = info
//        val registryJson = jsonEncoder.encodeToString(registry)
//
//        registryJson.writeFileContent(registryPath){
//            onError {
//                throw it.throwable
//            }
//        }
//    }
//
//    fun deleteAsset(asset: AssetInfo): Boolean {
//        var result = false
//        val entry = registry.assets.entries.firstOrNull { it.value.filePath == asset.filePath }
//        if (entry != null) {
//            registry.assets.remove(entry.key)
//            val registryJson = jsonEncoder.encodeToString(registry)
//            registryJson.writeFileContent(registryPath) {
//                onError {
//                    it.throwable.output()
//                }
//                onSuccess {
//                    result = true
//                    it
//                }
//            }
//        }
//        return result
//    }

}
package po.misc.configs.assets

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import po.misc.context.Component
import po.misc.data.logging.Verbosity
import po.misc.io.deleteFile
import po.misc.io.toSafePathName
import kotlin.enums.enumEntries


sealed interface AssetBuilder: ConfigHolder{
    private val selfie: AssetManager get() = this as AssetManager
    override val config: AssetManager.ConfigData

    fun buildRegistry(category: String, builder: AssetRegistry.(String)-> Unit){
       val registry = AssetRegistry(selfie, category)
       registry.loadOrCreate()
       registry.builder(category)
       selfie.addFilteringByConfig(config, registry)
    }

    fun <R> buildRegistry(category: Enum<*>, builder: AssetRegistry.(Enum<*>)-> R):R{
        val registry = AssetRegistry(selfie, category.name)
        registry.loadOrCreate()
        val lambdaResult = registry.builder(category)
        selfie.addFilteringByConfig(config, registry)
        return lambdaResult
    }


    fun List<String>.buildFromList(builder: AssetRegistry.(String)-> Unit){
        forEach {
            val registry = AssetRegistry(selfie, it)
            registry.loadOrCreate()
            registry.builder(it)
            selfie.addFilteringByConfig(config, registry)
        }
    }
}

inline fun <reified E: Enum<E>> AssetBuilder.buildFromEnum(
    noinline builder: AssetRegistry.(E)-> Unit
): List<AssetRegistry> {
    val selfie = this as AssetManager
    enumEntries<E>().forEach {
        val registry = AssetRegistry(selfie, it.name)
        registry.config = config
        registry.loadOrCreate()
        registry.builder(it)
        addFilteringByConfig(registry.config, registry)
    }
    return selfie.registriesBacking
}


/**
 * @param basePath relative path to the root folder
 */
class AssetManager(
    basePath: String,
    val jsonEncoder: Json,
): Component, AssetBuilder {

    data class ConfigData(
        var basePath: String,
        var recreateKeysChanged: Boolean = true,
        var verbosity: Verbosity = Verbosity.Info,
        var includeEmpty: Boolean = false,
        var clearEmptyOnInit: Boolean = false,
    ){
        fun setBasePath(relativePath: String):ConfigData{
            basePath = relativePath
            return this
        }
    }

    override var config: ConfigData = ConfigData(basePath = basePath)

    @PublishedApi
    internal val registriesBacking: MutableList<AssetRegistry> = mutableListOf()

    constructor(
        basePath: String,
        json: Json,
        configBuilder: AssetBuilder.()-> Unit
    ):this(basePath, json){
        configBuilder()
    }

    constructor(
        assetsConfig: AssetsKeyConfig,
        json: Json,
        configBuilder: AssetBuilder.()-> Unit
    ):this(
        assetsConfig.assetsPath,
        json
    ){
        configBuilder()
    }

    override var verbosity: Verbosity
        set(value) {  config.verbosity = value }
        get() =  config.verbosity

    override val componentName: String = "AssetManager"
    private val initSubject: String = "$componentName Initialization"
    private val operationsSubject: String = "$componentName Update"
    val registries : List<AssetRegistry> get() = registriesBacking

    internal fun createPath(category: String): String{
       return "${config.basePath}/${category.toSafePathName()}.json"
    }

    @PublishedApi
    internal fun addFilteringByConfig(config: ConfigData, registry: AssetRegistry){

        if(config.includeEmpty){
            registriesBacking.add(registry)
        }else{
            if(registry.assets.isNotEmpty()){
                registriesBacking.add(registry)
            }else{
                info(initSubject, "Registry $registry filtered out by config includeEmpty: ${config.includeEmpty}")
            }
        }
        if(config.clearEmptyOnInit){
            if(registry.assets.isEmpty()) {
                registry.purge()
                deleteFile(registry.registryPath)
                info(
                    initSubject,
                    "Registry $registry cleared out by config clearEmptyOnInit: ${config.clearEmptyOnInit}"
                )
            }
        }
    }

    fun initialize(){
        var commits = 0
        registries.forEach {
            if(it.commit()){
                commits ++
            }
        }
        if(commits > 0){
            info(initSubject, "Commited $commits registry changes")
        }else{
            info(initSubject, "No changes in registries nothing to commit")
        }
    }

    fun getByCategory(category: String): AssetRegistry?{
           return registries.firstOrNull{ it.category == category }
    }

    fun getByCategory(category: Enum<*>): AssetRegistry? = getByCategory(category.name)

    fun purge(category: String) : Boolean{
       return registries.firstOrNull{ it.category ==  category}?.let {
            it.purge()
            true
        }?:false
    }
    fun purge(registry: AssetRegistry): Boolean = purge(registry.category)


}
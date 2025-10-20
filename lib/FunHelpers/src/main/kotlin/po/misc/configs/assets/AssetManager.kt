package po.misc.configs.assets

import kotlinx.serialization.json.Json
import po.misc.context.component.Component
import po.misc.context.component.managedException
import po.misc.data.TextBuilder
import po.misc.data.logging.Verbosity
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.colorize
import po.misc.functions.Throwing
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
): Component, AssetBuilder, TextBuilder {

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


    private val initSubject: String = "${componentID.name} Initialization"
    private val operationsSubject: String = "$componentID Update"
    val registries : List<AssetRegistry> get() = registriesBacking

    private val registriesText: String get() = registries.joinToString(separator = SpecialChars.NEW_LINE) {
        it.toString()
    }

    private val notFoundMessage : (String) -> String = {
        "Registry with $it no found".newLine {
            "[Registry list]".colorize(Colour.Blue).newLine { registriesText.colorize(Colour.WhiteBright) }
        }
    }

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

    fun getRegistry(category: String): AssetRegistry?{
          return registries.firstOrNull{ it.category == category }
    }
    fun getRegistry(category: Enum<*>): AssetRegistry? = getRegistry(category.name)

    fun getRegistry(throwing: Throwing,  category: String): AssetRegistry{
       return getRegistry(category).getOrThrow {
            warn("Operations", notFoundMessage(category))
            managedException("Registry $category not found")
        }
    }
    fun getRegistry(throwing: Throwing, category: Enum<*>): AssetRegistry = getRegistry(throwing, category.name)

    fun withRegistry(category: Enum<*>, block: AssetRegistry.()-> Unit){
        val registry = getRegistry(category.name)
        if(registry == null){
            warn("Operations", notFoundMessage(category.name))
        }else{
            registry.block()
        }
    }
    fun withRegistry(throwing: Throwing,category: Enum<*>, block: AssetRegistry.()-> Unit){
        getRegistry(throwing, category).block()
    }

    fun getAsset(category: Enum<*>, assetName: NamedAsset): Asset?{
       return getRegistry(category)?.get(assetName)
    }

    fun getAsset(throwing: Throwing,  category: Enum<*>, assetName: NamedAsset): Asset{
        return getRegistry(throwing, category).get(throwing, assetName)
    }

    fun purge(category: String) : Boolean{
       return registries.firstOrNull{ it.category ==  category}?.let {
            it.purge()
            true
        }?:false
    }
    fun purge(registry: AssetRegistry): Boolean = purge(registry.category)


}
package po.test.misc.configs.assets

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import po.misc.configs.assets.AssetManager
import po.misc.configs.assets.buildFromEnum
import po.misc.functions.Throwing
import po.misc.io.readFile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestAssetManager {

    private val json = Json{
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = false
    }

    @Serializable
    internal enum class AssetCategory{
        Photo,
        Text,
        Config,
        Html
    }

    private val image1Path = "files/1.png"

    @Test
    fun `Registries properly initialized by enum values`(){
        val manager = AssetManager("assets", json)
        manager.buildFromEnum<AssetCategory> { category ->
            clearEmptyOnInit = true
            when (category) {
                AssetCategory.Photo -> {
                    addAsset(Throwing, readFile(image1Path), name = "no_image")
                    addAsset(Throwing, readFile("files/photo.png"))
                }
                AssetCategory.Html -> {
                    addAsset(Throwing, readFile("files/test.html"), name = "test_html_doc")
                }
                else -> {

                }
            }
        }
        manager.initialize()
        assertEquals(2,  manager.registries.size)
        val photos =  assertNotNull(manager.getByCategory(AssetCategory.Photo))
        assertEquals(2, photos.size)
        val html =  assertNotNull(manager.getByCategory(AssetCategory.Html))
        assertEquals(1, html.size)
    }

    @Test
    fun `Registries properly initialized by single enum`(){
        val manager = AssetManager("assets", json)
        val asset = manager.buildRegistry(AssetCategory.Photo){
            purge()
            addAsset(Throwing, readFile(image1Path), name = "no_image")
            addAsset(Throwing,readFile("files/photo.png"))
            assets
        }
        assertEquals(1,  manager.registries.size)
        val photos =  assertNotNull(manager.getByCategory(AssetCategory.Photo))
        assertEquals(2, photos.size)
        assertEquals(2, asset.size)
    }
}
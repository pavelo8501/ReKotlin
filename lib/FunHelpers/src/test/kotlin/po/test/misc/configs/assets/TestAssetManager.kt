package po.test.misc.configs.assets

import org.junit.jupiter.api.Test
import po.misc.configs.assets.asset.Asset
import po.misc.configs.assets.AssetManager
import po.misc.configs.assets.buildFromEnum
import po.misc.io.deleteAllOrNan
import po.misc.io.deleteFile
import po.misc.io.readFile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestAssetManager: AssetTest() {

    internal enum class AssetCategory{
        Photo,
        Text,
        Config,
        Html
    }

    private val image1Path = "files/1.png"


    @Test
    fun `Registries properly initialized by enum values`(){
        deleteAllOrNan{
            addPath(AssetManager.toAssetsPath(basePath, AssetCategory.Photo))
            addPath(AssetManager.toAssetsPath(basePath, AssetCategory.Html))
        }
        val manager = AssetManager(basePath, json)
        manager.buildFromEnum<AssetCategory> { category ->
            clearEmptyOnInit = true
            when (category) {
                AssetCategory.Photo -> {
                    addAsset(readFile(image1Path), name = "no_image")
                    addAsset(readFile("files/photo.png"), "photo")
                }
                AssetCategory.Html -> {
                    addAsset(readFile("files/test.html"), name = "test_html_doc")
                }
                else -> {

                }
            }
        }
        assertEquals(2,  manager.registries.size)
        val photos =  assertNotNull(manager.getRegistry(AssetCategory.Photo))
        assertEquals(2, photos.assets.size)
        photos.assets.values.forEach {
            assertEquals(Asset.State.Updated, it.state)
        }

        val html =  assertNotNull(manager.getRegistry(AssetCategory.Html))
        assertEquals(1, html.assets.size)
        html.assets.values.forEach {
            assertEquals(Asset.State.Updated, it.state)
        }
        manager.commitChanges()
        photos.assets.values.forEach {
            assertEquals(Asset.State.InSync, it.state)
        }
        html.assets.values.forEach {
            assertEquals(Asset.State.InSync, it.state)
        }
        assertTrue {
            val json =  readFile(photos.registryPath).readText()
            json.contains("no_image")&&
                    json.contains("photo")
        }
        assertTrue {
            val json = readFile(html.registryPath).readText()
            json.contains("test_html_doc")
        }
    }

    @Test
    fun `Registries properly initialized by single enum`(){

        deleteFile(AssetManager.toAssetsPath(basePath, AssetCategory.Photo))

        val manager = AssetManager(basePath, json)
        val asset = manager.buildRegistry(AssetCategory.Photo){
            addAsset(readFile(image1Path), name = "no_image")
            addAsset(readFile("files/photo.png"), "photo")
            assets
        }
        assertEquals(1,  manager.registries.size)
        val photos =  assertNotNull(manager.getRegistry(AssetCategory.Photo))
        assertEquals(2, photos.assets.size)
        assertEquals(2, asset.size)
    }
}
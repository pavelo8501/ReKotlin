package po.test.misc.configs.assets

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import po.misc.configs.assets.Asset
import po.misc.configs.assets.AssetManager
import po.misc.configs.assets.AssetRegistry
import po.misc.functions.Throwing
import po.misc.io.WriteOptions
import po.misc.io.readFile
import po.misc.io.readFileContent
import po.misc.io.readToString
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertTrue

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

    val registries = listOf(
        AssetRegistry(AssetCategory.Photo),
        AssetRegistry(AssetCategory.Text),
        AssetRegistry(AssetCategory.Config),
        AssetRegistry(AssetCategory.Html)
    )

    @Test
    fun `Asset registries successfully loaded even if file does not exists`(){
        val photoRegistry = AssetRegistry(AssetCategory.Photo)
        val manager =  AssetManager("assets", json)
        val loadedRegistry =  manager.applyRegistry(photoRegistry)
        assertNotNull(loadedRegistry)
        assertNotSame(photoRegistry, loadedRegistry)
    }

    @Test
    fun `Asset added result in registry update and save to FS`(){
        val photoRegistry = AssetRegistry(AssetCategory.Photo)
        val manager =  AssetManager("assets", json)
        val loadedRegistry = assertNotNull(manager.applyRegistry(photoRegistry))
        val asset = Asset("photo1", "files/1.jpg")

        loadedRegistry.add(asset)

        assertEquals(1,  loadedRegistry.assets.size)
        val jsonString  = assertDoesNotThrow {
            readFileContent("assets/${AssetCategory.Photo.name.lowercase()}.json").readToString()
        }
        assertTrue {
            jsonString.contains(asset.name) && jsonString.contains(asset.filePath)
        }
    }

    @Test
    fun `Asset Load local files as they should`(){
        val asset = Asset("photo1", "files/1.png")
        val file = asset.loadFile()
        assertNotNull(file)
    }

    @Test
    fun `Asset save files updating`(){
        val asset = Asset("photo1", "files/1.png")
        var updatedAsset: Asset? = null
        asset.onUpdated{
            updatedAsset = it
        }
        asset.saveFile(readFileContent("files/photo.png"), WriteOptions(overwriteExistent = true))
        val updated = assertNotNull(updatedAsset)
        val fileInfo = assertNotNull(updated.file)
        assertEquals("1.png", fileInfo.fileName)
    }

    @Test
    fun `Manager createOrLoad method does not overwrite existent assets`(){
        val manager =  AssetManager("assets", json)
        val registered = manager.applyRegistries(registries)
        assertEquals(registries.size, registered.size)
        val registeredAsset =  manager.createOrLoad("no_image", AssetCategory.Photo, readFile("files/1.png"))
        assertNotNull(registeredAsset)

        val manager2 =  AssetManager("assets", json)
        manager2.applyRegistries(registries)
        val registeredAsset2 =  manager2.createOrLoad("no_image", AssetCategory.Photo, readFile("files/photo.png"))
        val reRegistered = assertNotNull(registeredAsset2)
        assertTrue {
            reRegistered.filePath.contains("1.png")
        }
    }

    @Test
    fun `AssetRegistry builder method does not overwrite existent assets`(){

        val photoRegistry =  registries.first{ it.equals(AssetCategory.Photo) }
        var asset = photoRegistry.buildOrGet {
             Asset("no_image", "some path")
        }
        assertNull(asset)
        val manager = AssetManager("assets", json)
        val registeredPhoto = manager.applyRegistry(photoRegistry, Throwing)
        asset = registeredPhoto.buildOrGet {
            Asset("no_image", "some path")
        }
        val registered = assertNotNull(asset)
        assertTrue {
            registered.filePath.contains("1.png")
        }
    }

}
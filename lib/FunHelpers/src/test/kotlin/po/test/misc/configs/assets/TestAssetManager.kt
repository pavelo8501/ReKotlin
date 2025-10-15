package po.test.misc.configs.assets

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import po.misc.configs.assets.Asset
import po.misc.configs.assets.AssetManager
import po.misc.configs.assets.AssetRegistry
import po.misc.configs.assets.first
import po.misc.configs.assets.purge
import po.misc.functions.Throwing
import po.misc.io.WriteOptions
import po.misc.io.readFile
import po.misc.io.readFileContent
import po.misc.io.readToString
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame
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
        assertSame(photoRegistry, loadedRegistry)
    }

    @Test
    fun `Asset added result in registry update and save to FS`(){
        val photoRegistry = AssetRegistry(AssetCategory.Photo)
        val manager =  AssetManager("assets", json)
        manager.purge(photoRegistry)

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
        val purged = assertNotNull(manager.purge(AssetCategory.Photo))

        val registeredAsset =  manager.createOrLoad("no_image", AssetCategory.Photo, readFile("files/1.png"))
        assertNotNull(registeredAsset)
        assertTrue {
            registeredAsset.filePath.contains("files/1.png")
        }
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
        val manager = AssetManager("assets", json)
        manager.purge(photoRegistry)

         manager.applyRegistry(photoRegistry){
            buildOrGet {
                Asset("no_image", "some path")
            }
        }
        val asset = assertNotNull(photoRegistry.assets.values.firstOrNull())
        assertTrue {
            asset.filePath.contains("some path")
        }
       val noImage2 = photoRegistry.buildOrGet {
            Asset("no_image", "other path")
        }
        val reRegistered = assertNotNull(noImage2)
        assertTrue {
            reRegistered.filePath.contains("some path")
        }
    }

    @Test
    fun `AssetManager builder method does not overwrite existent assets`() {
        val manager = AssetManager("assets", json)
        val foundRegistry = registries.first(AssetCategory.Photo)

         manager.applyRegistry(foundRegistry , Throwing) {
            buildOrGet {
                Asset("no_image", "some path")
            }
            buildOrGet {
                Asset("other_asset", "other_path")
            }
        }
        assertTrue {
            foundRegistry.assets.size >= 2
        }
        val noImageAsset = assertNotNull(foundRegistry["no_image"])
        val otherAsset = assertNotNull(foundRegistry["other_asset"])
        assertTrue {
            noImageAsset.filePath.contains("1.png") &&
                    otherAsset.filePath.contains("other_path")
        }
    }

    @Test
    fun `AssetManager remove methods (member and attached) work same way and as expected`() {

        val manager = AssetManager("assets", json)

        val htmlRegistry = manager.applyRegistry(AssetRegistry(AssetCategory.Html), Throwing) {
            buildOrGet {
                Asset("no_image", "some path")
            }
            buildOrGet {
                Asset("other_asset", "other_path")
            }
        }
        assertTrue {
            htmlRegistry.assets.isNotEmpty()
        }
        val html = manager.purge(AssetCategory.Html)
        val clean = assertNotNull(html)
        assertEquals(0, clean.assets.size)

        manager.applyRegistry(htmlRegistry, Throwing) {
            buildOrGet {
                Asset("no_image", "some path")
            }
            buildOrGet {
                Asset("other_asset", "other_path")
            }
        }
        assertEquals(2, htmlRegistry.assets.size)
        val html2 = manager.purge(htmlRegistry)
        val clean2 = assertNotNull(html2)
        assertEquals(0, clean2.assets.size)
    }

}
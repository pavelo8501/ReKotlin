package po.test.misc.io

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import po.misc.io.deleteAllOrNan
import po.misc.io.fileExists
import po.misc.io.readFile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestFileDelete {


    private val file1Path = "files/1.png"
    private val file2Path = "files/photo.png"

    val file1 = readFile(file1Path)
    val file2 = readFile(file2Path)


    @AfterEach
    fun returnFilesBack(){
        file1.rewrite()
        file2.rewrite()
    }

    @Test
    fun `Test bulk delete happy path`() {
        val result = deleteAllOrNan {
            addPath(file1Path)
            addPath(file2Path)
        }
        assertEquals(true, result)
    }


    @Test
    fun `Test bulk delete one path wrong`() {
        val result = deleteAllOrNan {
            addPath(file1Path)
            addPath("files/photo1.png")
        }
        assertEquals(false, result)

        assertNotNull(fileExists(file1Path))
        assertNotNull(fileExists(file2Path))
    }

}
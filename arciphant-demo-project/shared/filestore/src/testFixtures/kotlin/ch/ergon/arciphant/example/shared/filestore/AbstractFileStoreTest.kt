package ch.ergon.arciphant.example.shared.filestore

import ch.ergon.arciphant.example.shared.api.Id
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

abstract class AbstractFileStoreTest<F: FileStore, ID: Id>(
    private val fileStore: F,
    private val addFileFn: F.(ID, File) -> Unit,
    private val getFileFn: F.(ID) -> File,
    private val id: ID,
) {

    @Test
    fun `it should persist file`() {
        val file = File.createTempFile("test", ".txt")

        fileStore.addFileFn(id, file)

        val loadedFile = fileStore.getFileFn(id)

        assertThat(file.name).isEqualTo(loadedFile.name)

    }

}

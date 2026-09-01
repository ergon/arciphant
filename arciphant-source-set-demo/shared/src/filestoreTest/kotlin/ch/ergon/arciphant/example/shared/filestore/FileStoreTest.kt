package ch.ergon.arciphant.example.shared.filestore

import ch.ergon.arciphant.example.shared.api.Id
import java.util.*

class FileStoreTest : AbstractFileStoreTest<TestFileStore, TestId>(
    TestFileStore(),
    TestFileStore::addFile,
    TestFileStore::getFile,
    TestId,
)

data object TestId : Id {
    override val value: UUID = UUID.randomUUID()
}

class TestFileStore : FileStore()

package ch.ergon.arciphant.example.shared.filestore

import ch.ergon.arciphant.example.shared.api.Id
import java.io.File

abstract class FileStore {

    private val filesById = mutableMapOf<Id, File>()

    fun addFile(id: Id, file: File) {
        filesById[id] = file
    }

    fun getFile(id: Id): File {
        return filesById.getValue(id)
    }

}

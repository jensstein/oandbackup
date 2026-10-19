package com.machiav3lli.backup.data.repository

import com.machiav3lli.backup.SELECTIONS_FOLDER_NAME
import com.machiav3lli.backup.data.entity.StorageFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent

class SelectionsRepository(
    private val backupRoot: StorageFile?
) : KoinComponent {
    suspend fun getSelections(): List<SavedSelection>? = withContext(Dispatchers.IO) {
        val selectionsDir = backupRoot?.findFile(SELECTIONS_FOLDER_NAME)
            ?: backupRoot?.createDirectory(SELECTIONS_FOLDER_NAME)

        selectionsDir?.listFiles()
            ?.mapNotNull { file ->
                file.name?.let { SavedSelection(it, file) }
            }
    }

    suspend fun loadSelection(name: String): Set<String> = withContext(Dispatchers.IO) {
        val selectionsDir = backupRoot?.findFile(SELECTIONS_FOLDER_NAME)
        selectionsDir?.findFile(name)?.readText()?.lines()?.toSet() ?: emptySet()
    }

    suspend fun saveSelection(name: String, packages: Set<String>) = withContext(Dispatchers.IO) {
        backupRoot?.let { root ->
            val selectionsDir = root.ensureDirectory(SELECTIONS_FOLDER_NAME)
            selectionsDir.createFile(name).writeText(packages.joinToString("\n"))
        }
    }

    suspend fun deleteSelection(name: String) = withContext(Dispatchers.IO) {
        val selectionsDir = backupRoot?.findFile(SELECTIONS_FOLDER_NAME)
        selectionsDir?.findFile(name)?.delete()
    }

    data class SavedSelection(
        val name: String,
        val file: StorageFile
    )
}
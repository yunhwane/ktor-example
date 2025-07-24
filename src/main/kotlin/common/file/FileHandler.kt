package com.example.common.file

import com.example.common.exception.CustomException
import com.example.common.exception.ErrorCode
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import java.io.ByteArrayOutputStream


object FileHandler {
    private const val chunkSize = 1024 * 1024

    fun handlingIncomingFile(item: PartData.FileItem): Pair<String, ByteArray> {
        try {
            val fileDataStream = ByteArrayOutputStream()

            val inputStream = item.streamProvider()

            val buffer = ByteArray(chunkSize)
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                fileDataStream.write(buffer, 0, bytesRead)
            }

            val fileData = fileDataStream.toByteArray()
            return Pair(item.originalFileName ?: "UnNamed-file", fileData)
        } catch (e: Exception) {
            throw CustomException(ErrorCode.FAILED_TO_HANDLING_FILE, e.message.toString())
        } finally {
            item.dispose()
        }
    }


    suspend fun exportFileData(forms: MultiPartData): Pair<PartData.FileItem, String> {
        var fileItem: PartData.FileItem? = null
        var jsonData = ""

        forms.forEachPart { part ->
            when {
                part is PartData.FileItem && part.name == "file" -> {
                    fileItem = part
                }

                part is PartData.FormItem && part.name == "jsonData" -> {
                    jsonData = part.value
                    part.dispose()
                }

                else -> part.dispose()
            }
        }

        if (fileItem == null) {
            throw CustomException(ErrorCode.FILE_NOT_FOUND)
        }

        if (jsonData.isBlank()) {
            throw CustomException(ErrorCode.INVALID_REQUEST_FORMAT, "JSON Data is empty")
        }

        return Pair(fileItem, jsonData)

    }
}
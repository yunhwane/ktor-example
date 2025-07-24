package com.example.common.file

import io.ktor.http.ContentType
import io.minio.BucketExistsArgs
import io.minio.GetPresignedObjectUrlArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.http.Method
import java.io.ByteArrayInputStream

object FileStorage {

    private lateinit var client : MinioClient
    private lateinit var bucket : String

    fun initialize(
        bucket : String,
        endPoint : String, access : String, secret : String,
    ) {
        this.bucket = bucket

        val builder = MinioClient.builder()
        builder.endpoint(endPoint)
        builder.credentials(access, secret)

        this.client = builder.build()

        verifyBucket(bucket)
    }

    fun uploadFile(fileBytes : ByteArray, fileName : String, filePath : String) : String {
        val contentType = contentType(fileName)

        client.putObject(
            PutObjectArgs.builder()
                .bucket(this.bucket)
                .`object`(filePath)
                .stream(ByteArrayInputStream(fileBytes), fileBytes.size.toLong(), -1)
                .contentType(contentType)
                .build()
        )

        return fileName
    }

    fun getFileUrl(filePath : String) : String{
        return client.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .bucket(this.bucket)
                .`object`(filePath)
                .method(Method.GET)
                .build()
        )
    }

    fun filePathMaker(vararg fileNameComponents: String, seperator : String = "/") : String {
        return fileNameComponents.filter { it.isNotBlank() }.joinToString(seperator)
    }

    private fun verifyBucket(bucket : String) {
        val bucketExist = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())
        if (!bucketExist) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build())
        }
    }

    private fun contentType(fileName: String): String {
        return when {
            fileName.endsWith(".jpg", true) || fileName.endsWith(".jpeg", true) -> ContentType.Image.JPEG.toString()
            fileName.endsWith(".png", true) -> ContentType.Image.PNG.toString()
            fileName.endsWith(".pdf", true) -> ContentType.Application.Pdf.toString()
            fileName.endsWith(".txt", true) -> ContentType.Text.Plain.toString()
            fileName.endsWith(".mp4", true) -> ContentType.Video.MP4.toString()
            fileName.endsWith(".mp3", true) -> ContentType.Audio.MPEG.toString()
            else -> ContentType.Application.OctetStream.toString()
        }
    }

}
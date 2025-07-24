package com.example.router.capsule.service

import com.example.common.exception.CustomException
import com.example.common.exception.ErrorCode
import com.example.common.file.FileStorage
import com.example.common.transactional.TransactionProvider
import com.example.config.EmailConfig
import com.example.repository.CapsuleContentRepository
import com.example.repository.CapsuleFileKeyRepository
import com.example.repository.CapsuleRepository
import com.example.repository.RecipientsRepository
import com.example.repository.TimeEncryptionMapperRepository
import com.example.repository.UserRepository
import com.example.router.capsule.types.CapsuleCreateResponse
import com.example.router.capsule.types.OpenCapsuleResponse
import com.example.security.TimeBaseEncryptionProvider
import com.example.security.TimeLockedData
import com.example.types.GlobalResponse
import com.example.types.GlobalResponseProvider
import com.example.types.storage.CapsuleStatus
import com.example.types.storage.ContentType
import com.example.types.wire.CapsuleWire

class CapsuleService (
    private val capsuleRepository: CapsuleRepository,
    private val timeEncryptionMapperRepository: TimeEncryptionMapperRepository,
    private val capsuleContentRepository: CapsuleContentRepository,
    private val recipientsRepository: RecipientsRepository,
    private val fileKeyRepository: CapsuleFileKeyRepository,
    private val userRepository: UserRepository,
    private val emailProvider : EmailConfig.EmailServiceImpl
){

    suspend fun openCapsuleContent(capsuleId : String) : GlobalResponse<OpenCapsuleResponse> {
        val openDate = TransactionProvider.transaction {
            capsuleRepository.openDateByCapsuleId(capsuleId)
        } ?: throw CustomException(ErrorCode.FAILED_TO_QUERY)

        // TODO -> 현재 시간이 openDate보다 큰지

        val recipientEmail = TransactionProvider.transaction {
            recipientsRepository.recipientEmailByCapsuleId(capsuleId)
        }

        var notiSended = false

        try {
            emailProvider.sendEmail(
                recipientEmail,
                "Your Time Capsule is opend!!",
                """
                    <html>
                    <body>
                        <h1>Your Time Capsule Is Ready!</h1>
                        <p>Great news! The time capsule is now available to open.</p>
                        <p>This capsule was sealed on ${openDate} 
                        and contains memories waiting for you to rediscover.</p>
                        <p>Enjoy your journey back in time!</p>
                        <p>Best regards,<br>The Time Capsule Team</p>
                    </body>
                    </html>
                    """.trimIndent()
            )
            notiSended = true
        } catch (e :Exception) {
            // TODO -> logger
            print(e.message)
        }

        val (key, data, timeSalt) = TransactionProvider.transaction {
            timeEncryptionMapperRepository.timeLockDataWhenOpen(capsuleId)
        }

        val timeLockData = TimeLockedData(
            encryptionContent = data,
            encryptionKey = key,
            releaseTime = openDate,
            timeSalt = timeSalt,
        )

        val dContent = TimeBaseEncryptionProvider.decrypt(timeLockData)

        TransactionProvider.transaction {
            capsuleContentRepository.updateContentById(capsuleId, dContent)
            capsuleRepository.updateSealStatusById(capsuleId, CapsuleStatus.opened)
            recipientsRepository.updateHasViewAndNotiSendedById(capsuleId, true, notiSended)
        }

        return GlobalResponseProvider.new(0, "", null)
    }

    suspend fun fileContent(
        userId : String,
        contentType: ContentType,
        email : String,
        title : String,
        description : String,
        content :String,
        openData: Int,
        file : ByteArray,
        fileName : String
    ) : GlobalResponse<CapsuleCreateResponse> {

        try {
            val encryptedData = TimeBaseEncryptionProvider.encrypt(content, openData)

            var capsuleId = ""
            var filePath = ""

            TransactionProvider.transaction {
                capsuleId = capsuleRepository.create(userId, title, description, openData)
                timeEncryptionMapperRepository.create(capsuleId, encryptedData.encryptionKey, encryptedData.timeSalt)
                capsuleContentRepository.create(capsuleId, contentType, encryptedData.encryptionContent)
                recipientsRepository.create(capsuleId, email)

                filePath = FileStorage.filePathMaker(userId, title, fileName)


                fileKeyRepository.create(capsuleId, filePath, fileName)
                FileStorage.uploadFile(file, fileName, filePath)
            }

            return GlobalResponseProvider.new(0, "SUCCESS", CapsuleCreateResponse(
                capsuleId = capsuleId,
                recipientEmail = email,
                contentType =  contentType,
                filePath = filePath,
                fileName = fileName,
                fileSize = file.size
            ))
        } catch (e : Exception) {
            return GlobalResponseProvider.new(-1, "failed to create capsule ${e.message}", null)
        }
    }

    suspend fun textContent(
        userID : String,
        email : String,
        title : String,
        description : String,
        content :String,
        openData: Int
    ) : GlobalResponse<CapsuleCreateResponse> {
        try {
            val encryptedData = TimeBaseEncryptionProvider.encrypt(content, openData)

            val capsuleId = TransactionProvider.transaction transactional@{
                val capsuleId = capsuleRepository.create(userID, title, description, openData)
                timeEncryptionMapperRepository.create(capsuleId, encryptedData.encryptionKey, encryptedData.timeSalt)
                capsuleContentRepository.create(capsuleId, ContentType.text, encryptedData.encryptionContent)
                recipientsRepository.create(capsuleId, email)
                return@transactional capsuleId
            }

            return GlobalResponseProvider.new(0, "SUCCESS", CapsuleCreateResponse(
                capsuleId = capsuleId,
                recipientEmail = email,
                contentType = ContentType.text
            ))
        } catch (e : Exception) {
            return GlobalResponseProvider.new(-1, "failed to create capsule ${e.message}", null)
        }
    }

    suspend fun capsuleContentById(capsuleId : String) : GlobalResponse<CapsuleWire?> {
        try {
            val result : CapsuleWire? = TransactionProvider.transaction {
                capsuleRepository.capsuleContnetWithRecipient(capsuleId)?.toWire()
            }
            return GlobalResponseProvider.new(0, "", result)
        } catch (e: Exception) {
            throw CustomException(ErrorCode.FAILED_TO_QUERY, e.message.toString())
        }
    }

    suspend fun verifyEmailExist(email : String) : Boolean {
        return TransactionProvider.transaction {
            userRepository.existsByEmail(email)
        }
    }
}
package com.example.common.di

import com.example.repository.CapsuleContentRepository
import com.example.repository.CapsuleFileKeyRepository
import com.example.repository.CapsuleRepository
import com.example.repository.RecipientsRepository
import com.example.repository.TimeEncryptionMapperRepository
import com.example.repository.UserRepository
import com.example.repository.UserTokenMapperRepository
import com.example.router.auth.service.AuthService
import com.example.router.capsule.service.CapsuleService
import org.koin.dsl.module

val additionalModule = module {

}

var repositoryModule = module {
    single { UserRepository() }
    single { UserTokenMapperRepository() }
    single { TimeEncryptionMapperRepository() }
    single { CapsuleContentRepository() }
    single { CapsuleFileKeyRepository() }
    single { CapsuleRepository() }
    single { RecipientsRepository() }
}


var serviceModule = module {
    single { AuthService(get(), get()) }

    single {
        CapsuleService(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}

val appModule = module {
    includes(additionalModule)
    includes(repositoryModule)
    includes(serviceModule)
}
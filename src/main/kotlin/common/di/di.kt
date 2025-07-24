package com.example.common.di

import com.example.repository.UserRepository
import com.example.repository.UserTokenMapperRepository
import com.example.router.auth.service.AuthService
import org.koin.dsl.module

val additionalModule = module {

}

var repositoryModule = module {
    single { UserRepository() }
    single { UserTokenMapperRepository() }
}

var serviceModule = module {
    single { AuthService(get(), get()) }
}

val appModule = module {
    includes(additionalModule)
    includes(repositoryModule)
    includes(serviceModule)
}
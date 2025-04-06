package com.mhez_dev.rentabols_v1.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mhez_dev.rentabols_v1.data.repository.FirebaseAuthRepository
import com.mhez_dev.rentabols_v1.data.repository.FirebaseRentalRepository
import com.mhez_dev.rentabols_v1.domain.repository.AuthRepository
import com.mhez_dev.rentabols_v1.domain.repository.RentalRepository
import com.mhez_dev.rentabols_v1.domain.usecase.auth.GetCurrentUserUseCase
import com.mhez_dev.rentabols_v1.domain.usecase.auth.SignInUseCase
import com.mhez_dev.rentabols_v1.domain.usecase.auth.SignUpUseCase
import com.mhez_dev.rentabols_v1.domain.usecase.rental.*
import com.mhez_dev.rentabols_v1.presentation.auth.AuthViewModel
import com.mhez_dev.rentabols_v1.presentation.home.HomeViewModel
import com.mhez_dev.rentabols_v1.presentation.items.ItemDetailsViewModel
import com.mhez_dev.rentabols_v1.presentation.map.MapViewModel
import com.mhez_dev.rentabols_v1.presentation.profile.ProfileViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Firebase
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single { FirebaseStorage.getInstance() }
    
    // Repositories
    single<AuthRepository> { FirebaseAuthRepository(get(), get()) }
    single<RentalRepository> { FirebaseRentalRepository(get()) }
    
    // Auth Use Cases
    single { SignInUseCase(get()) }
    single { SignUpUseCase(get()) }
    single { GetCurrentUserUseCase(get()) }
    
    // Rental Use Cases
    single { CreateRentalItemUseCase(get()) }
    single { SearchRentalItemsUseCase(get()) }
    single { GetRentalItemsUseCase(get()) }
    single { CreateRentalRequestUseCase(get()) }
    single { GetUserTransactionsUseCase(get()) }
    
    // ViewModels
    viewModel { AuthViewModel(get(), get(), get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { ItemDetailsViewModel(get(), get()) }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { MapViewModel(get()) }
}

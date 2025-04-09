package com.mhez_dev.rentabols_v1.presentation.onboarding

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val preferences: SharedPreferences
) : ViewModel() {
    private val _shouldShowOnboarding = MutableStateFlow(true)
    val shouldShowOnboarding: StateFlow<Boolean> = _shouldShowOnboarding
    
    init {
        viewModelScope.launch {
            // Reset onboarding status for testing
            preferences.edit().putBoolean(KEY_ONBOARDING_COMPLETED, false).apply()
            checkIfOnboardingCompleted()
        }
    }
    
    private fun checkIfOnboardingCompleted() {
        val onboardingCompleted = preferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        _shouldShowOnboarding.value = !onboardingCompleted
    }
    
    fun onOnboardingFinished() {
        preferences.edit().putBoolean(KEY_ONBOARDING_COMPLETED, true).apply()
        _shouldShowOnboarding.value = false
    }
    
    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        
        fun createPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences("rentabols_prefs", Context.MODE_PRIVATE)
        }
    }
} 
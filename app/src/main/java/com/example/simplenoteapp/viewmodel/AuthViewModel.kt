package com.example.simplenoteapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isAuthenticated: Boolean = false,
    val error: String? = null
)

class AuthViewModel : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    init {
        auth.addAuthStateListener {
            _authState.value = _authState.value.copy(isAuthenticated = it.currentUser != null)
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            _authState.value = _authState.value.copy(isAuthenticated = true, error = null)
                        } else {
                            _authState.value = _authState.value.copy(isAuthenticated = false, error = it.exception?.message)
                        }
                    }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(isAuthenticated = false, error = e.message)
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            _authState.value = _authState.value.copy(isAuthenticated = true, error = null)
                        } else {
                            _authState.value = _authState.value.copy(isAuthenticated = false, error = it.exception?.message)
                        }
                    }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(isAuthenticated = false, error = e.message)
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = _authState.value.copy(isAuthenticated = false, error = null)
    }
}

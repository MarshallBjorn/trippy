package com.navrotskyi.trippyapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.navrotskyi.trippyapp.api.TokenManager
import com.navrotskyi.trippyapp.ui.Screen
import com.navrotskyi.trippyapp.ui.screens.LoginScreen
import com.navrotskyi.trippyapp.ui.screens.RegisterScreen
import com.navrotskyi.trippyapp.ui.theme.TrippyAppTheme
import com.navrotskyi.trippyapp.ui.viewmodels.AuthState
import com.navrotskyi.trippyapp.ui.viewmodels.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TokenManager.init(this)
        enableEdgeToEdge()

        setContent {
            TrippyAppTheme {
                val navController = rememberNavController() // Serce nawigacji
                val authViewModel: AuthViewModel = viewModel()
                val authState = authViewModel.authState
                val context = LocalContext.current

                LaunchedEffect(authState) {
                    when (authState) {
                        is AuthState.Success -> {
                            TokenManager.saveToken(authState.token)
                            Toast.makeText(context, "Sukces!", Toast.LENGTH_SHORT).show()
                            authViewModel.resetState()

                            // Po zalogowaniu/rejestracji możesz przejść dalej:
                            // navController.navigate(Screen.Home.route)
                        }
                        is AuthState.Error -> {
                            Toast.makeText(context, authState.message, Toast.LENGTH_LONG).show()
                            authViewModel.resetState()
                        }
                        else -> {}
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Definiujemy mapę naszych ekranów
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Login.route, // Ekran startowy
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // Definicja ekranu logowania
                        composable(Screen.Login.route) {
                            LoginScreen(
                                onLoginClick = { email, password ->
                                    authViewModel.login(email, password)
                                },
                                onRegisterClick = {
                                    navController.navigate(Screen.Register.route) // Przełączamy ekran
                                },
                                modifier = Modifier
                            )
                        }

                        // Definicja ekranu rejestracji
                        composable(Screen.Register.route) {
                            RegisterScreen(
                                onRegisterClick = { name, email, password ->
                                    authViewModel.register(name, email, password)
                                },
                                onBackToLoginClick = {
                                    navController.popBackStack() // Wraca do poprzedniego ekranu
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
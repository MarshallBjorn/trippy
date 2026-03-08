package com.navrotskyi.trippyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.navrotskyi.trippyapp.ui.screens.LoginScreen
import com.navrotskyi.trippyapp.ui.screens.RegisterScreen
import com.navrotskyi.trippyapp.ui.theme.TrippyAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TrippyAppTheme {
                var showLoginScreen by remember { mutableStateOf(true) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    if (showLoginScreen) {
                        LoginScreen(
                            modifier = Modifier.padding(innerPadding),
                            onLoginClick = { email, password ->
                                println("Próba logowania: $email")
                                // TODO: Logika logowania
                            },
                            onRegisterClick = {
                                showLoginScreen = false
                            }
                        )
                    } else {
                        RegisterScreen(
                            modifier = Modifier.padding(innerPadding),
                            onRegisterClick = { name, email, password ->
                                println("Rejestracja: $name, $email")
                                // TODO: Logika rejestracji
                            },
                            onBackToLoginClick = {
                                showLoginScreen = true
                            }
                        )
                    }
                }
            }
        }
    }
}
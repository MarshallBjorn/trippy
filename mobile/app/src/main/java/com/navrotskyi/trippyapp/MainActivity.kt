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
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import com.navrotskyi.trippyapp.data.database.UserDb
import com.navrotskyi.trippyapp.ui.screens.profile.*
import com.navrotskyi.trippyapp.ui.viewmodels.ProfileViewModel
import com.navrotskyi.trippyapp.ui.viewmodels.ProfileViewModelFactory
import androidx.compose.runtime.Composable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TokenManager.init(this)
        enableEdgeToEdge()

        setContent {
            TrippyAppTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                val authState = authViewModel.authState
                val context = LocalContext.current

                //tutaj inicjalizujecie swoje viewmodele
                val userDao = remember { UserDb.getInstance(context).userDao() }
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = ProfileViewModelFactory(userDao)
                )

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                LaunchedEffect(authState) {
                    when (authState) {
                        is AuthState.Success -> {
                            TokenManager.saveToken(authState.token)
                            Toast.makeText(context, "Sukces!", Toast.LENGTH_SHORT).show()
                            authViewModel.resetState()

                            //tutaj zmieniasz ekran docelowy po zalogowaniu, na razie jest widok profilu
                            navController.navigate(Screen.Profile.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                        is AuthState.Error -> {
                            Toast.makeText(context, authState.message, Toast.LENGTH_LONG).show()
                            authViewModel.resetState()
                        }
                        else -> {}
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // Lista ekranów "głównych"
                        val rootScreens = listOf(Screen.Trips.route, Screen.Expenses.route, Screen.Profile.route)

                        if (currentRoute in rootScreens) {
                            NavigationBar(
                                containerColor = Color.White,
                                tonalElevation = 8.dp
                            ) {
                                NavigationBarItem(
                                    selected = currentRoute == Screen.Trips.route,
                                    label = { Text("Wycieczki") },
                                    icon = { Icon(Icons.Default.GridView, null) },
                                    onClick = { if (currentRoute != Screen.Trips.route) navController.navigate(Screen.Trips.route) },
                                    colors = navItemColors()
                                )
                                NavigationBarItem(
                                    selected = currentRoute == Screen.Expenses.route,
                                    label = { Text("Wydatki") },
                                    icon = { Icon(Icons.Default.AttachMoney, null) },
                                    onClick = { if (currentRoute != Screen.Expenses.route) navController.navigate(Screen.Expenses.route) },
                                    colors = navItemColors()
                                )
                                NavigationBarItem(
                                    selected = currentRoute == Screen.Profile.route,
                                    label = { Text("Profil") },
                                    icon = { Icon(Icons.Default.Person, null) },
                                    onClick = { if (currentRoute != Screen.Profile.route) navController.navigate(Screen.Profile.route) },
                                    colors = navItemColors()
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Login.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // Definicja ekranu logowania
                        composable(Screen.Login.route) {
                            LoginScreen(
                                onLoginClick = { email, password ->
                                    authViewModel.login(email, password)
                                },
                                onRegisterClick = {
                                    navController.navigate(Screen.Register.route) // Przełączenie ekranu
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
                        //EKRAN PROFILU
                        composable(Screen.Profile.route) {
                            ProfileScreen(
                                viewModel = profileViewModel,
                                onEditProfileClick = { navController.navigate(Screen.EditProfile.route) },
                                onCurrencyClick = { navController.navigate(Screen.Currency.route) },
                                onPasswordClick = { navController.navigate(Screen.ChangePassword.route) },
                                onLogoutClick = {
                                    TokenManager.clearToken()
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                        //EKRAN EDYCJI PROFILI
                        composable(Screen.EditProfile.route) {
                            val userState by profileViewModel.user.collectAsState()
                            EditProfileScreen(
                                currentName = userState?.name ?: "",
                                onSaveClick = { profileViewModel.updateUserName(it); navController.popBackStack() },
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        //EKRAN ZMIANY WALUTY
                        composable(Screen.Currency.route) {
                            val userState by profileViewModel.user.collectAsState()
                            val currencies by profileViewModel.currencies.collectAsState()
                            CurrencyScreen(
                                currentCurrency = userState?.currency ?: "PLN",
                                currencies = currencies,
                                onSaveClick = { profileViewModel.updateCurrency(it); navController.popBackStack() },
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        //EKRAN ZMIANY HASLA
                        composable(Screen.ChangePassword.route) {
                            ChangePasswordScreen(
                                onSaveClick = { _, newPass -> profileViewModel.updatePassword(newPass); navController.popBackStack() },
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        //EKRAN WYCIECZEK
                        composable(Screen.Trips.route) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Tu będą wycieczki - podmieńcie ten Box na swój ekran")
                            }
                        }
                        //EKRAN WYDATKOW
                        composable(Screen.Expenses.route) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Tu będą Wydatki - podmieńcie ten Box na swój ekran")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = Color(0xFF142E50),
    selectedTextColor = Color(0xFF142E50),
    indicatorColor = Color(0xFFE8ECEF),
    unselectedIconColor = Color.Gray,
    unselectedTextColor = Color.Gray
)


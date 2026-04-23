package com.navrotskyi.trippyapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import com.navrotskyi.trippyapp.api.TokenManager
import com.navrotskyi.trippyapp.data.database.UserDb
import com.navrotskyi.trippyapp.ui.Screen
import com.navrotskyi.trippyapp.ui.screens.GroupBalanceScreen
import com.navrotskyi.trippyapp.ui.screens.LoginScreen
import com.navrotskyi.trippyapp.ui.screens.RegisterScreen
import com.navrotskyi.trippyapp.ui.screens.journeys.AddNodeScreen
import com.navrotskyi.trippyapp.ui.screens.expenses.ExpensesScreen
import com.navrotskyi.trippyapp.ui.screens.journeys.TripDetailsScreen
import com.navrotskyi.trippyapp.ui.screens.journeys.InviteParticipantScreen
import com.navrotskyi.trippyapp.ui.screens.journeys.JourneysScreen
import com.navrotskyi.trippyapp.ui.screens.journeys.AddTripScreen
import com.navrotskyi.trippyapp.ui.screens.journeys.TripNodeDetailsScreen
import com.navrotskyi.trippyapp.ui.screens.journeys.EditNodeScreen
import com.navrotskyi.trippyapp.ui.screens.profile.*
import com.navrotskyi.trippyapp.ui.theme.TrippyAppTheme
import com.navrotskyi.trippyapp.ui.viewmodels.*

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

                // Inicjalizacja ViewModeli
                val userDao = remember { UserDb.getInstance(context).userDao() }
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = ProfileViewModelFactory(userDao)
                )
                val tripViewModel: TripViewModel = viewModel()
                val expensesViewModel: ExpensesViewModel = viewModel()

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                LaunchedEffect(authState) {
                    when (authState) {
                        is AuthState.Success -> {
                            TokenManager.saveToken(authState.token)
                            Toast.makeText(context, "Sukces!", Toast.LENGTH_SHORT).show()

                            tripViewModel.loadTrips()

                            profileViewModel.fetchUserFromApi()

                            authViewModel.resetState()

                            navController.navigate(Screen.Trips.route) {
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
                        val rootScreens = listOf(Screen.Trips.route, Screen.Expenses.route, Screen.Profile.route)

                        if (currentRoute in rootScreens) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
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
                        composable(Screen.Login.route) {
                            LoginScreen(
                                onLoginClick = { email, password -> authViewModel.login(email, password) },
                                onRegisterClick = { navController.navigate(Screen.Register.route) },
                                modifier = Modifier
                            )
                        }

                        composable(Screen.Register.route) {
                            RegisterScreen(
                                onRegisterClick = { name, email, password -> authViewModel.register(name, email, password) },
                                onBackToLoginClick = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Profile.route) {
                            ProfileScreen(
                                viewModel = profileViewModel,
                                onEditProfileClick = { navController.navigate(Screen.EditProfile.route) },
                                onCurrencyClick = { navController.navigate(Screen.Currency.route) },
                                onPasswordClick = { navController.navigate(Screen.ChangePassword.route) },
                                onLogoutClick = {
                                    profileViewModel.logout {
                                        TokenManager.clearToken()
                                        tripViewModel.clearData()
                                        navController.navigate(Screen.Login.route) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                        Toast.makeText(context, "Wylogowano bezpiecznie", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }

                        composable(Screen.EditProfile.route) {
                            val userState by profileViewModel.user.collectAsState()
                            EditProfileScreen(
                                currentName = userState?.name ?: "",
                                viewModel = profileViewModel,
                                onSaveClick = { newName ->
                                    profileViewModel.updateUserName(newName) { success ->
                                        if (success) {
                                            navController.popBackStack()
                                        }
                                    }
                                },
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Currency.route) {
                            val userState by profileViewModel.user.collectAsState()
                            val currencies by profileViewModel.currencies.collectAsState()
                            CurrencyScreen(
                                currentCurrency = userState?.currency ?: "PLN",
                                currencies = currencies,
                                viewModel = profileViewModel,
                                onSaveClick = { selectedCurrency ->
                                    profileViewModel.updateCurrency(selectedCurrency) { success ->
                                        if (success) {
                                            navController.popBackStack()
                                        }
                                    }
                                },
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.ChangePassword.route) {
                            ChangePasswordScreen(
                                viewModel = profileViewModel,
                                onSaveClick = { oldPass, newPass, onResult ->
                                    profileViewModel.updatePassword(oldPass, newPass) { success ->
                                        onResult(if (success) null else "Błąd")
                                    }
                                },
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Trips.route) {
                            JourneysScreen(
                                viewModel = tripViewModel,
                                onTripClick = { tripId -> navController.navigate(Screen.TripDetails.createRoute(tripId)) },
                                onAddTripClick = { navController.navigate(Screen.AddTrip.route) }
                            )
                        }

                        composable(Screen.AddTrip.route) {
                            AddTripScreen(
                                viewModel = tripViewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = Screen.TripDetails.route,
                            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
                            TripDetailsScreen(
                                tripId = tripId,
                                viewModel = tripViewModel,
                                profileViewModel = profileViewModel,
                                onBackClick = { navController.popBackStack() },
                                onInviteClick = { id -> navController.navigate(Screen.InviteParticipant.createRoute(id)) },
                                onAddNodeClick = { id -> navController.navigate(Screen.AddNode.createRoute(id)) },
                                onNodeClick = { nodeId -> navController.navigate(Screen.NodeDetails.createRoute(tripId, nodeId)) },
                                onGroupBalanceClick = {id -> navController.navigate(Screen.GroupBalance.createRoute(id))}
                            )
                        }

                        composable(
                            route = Screen.NodeDetails.route,
                            arguments = listOf(
                                navArgument("tripId") { type = NavType.StringType },
                                navArgument("nodeId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                            val nodeId = backStackEntry.arguments?.getString("nodeId") ?: ""
                            TripNodeDetailsScreen(
                                tripId = tripId,
                                nodeId = nodeId,
                                viewModel = tripViewModel,
                                profileViewModel = profileViewModel,
                                onBackClick = {
                                    navController.popBackStack()
                                    tripViewModel.clearSelectedNode()
                                },
                                onEditClick = { id ->
                                    navController.navigate(Screen.EditNode.createRoute(tripId, id))
                                }
                            )
                        }

                        composable(
                            route = Screen.EditNode.route,
                            arguments = listOf(
                                navArgument("tripId") { type = NavType.StringType },
                                navArgument("nodeId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                            val nodeId = backStackEntry.arguments?.getString("nodeId") ?: ""
                            EditNodeScreen(
                                tripId = tripId,
                                nodeId = nodeId,
                                viewModel = tripViewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = Screen.AddNode.route,
                            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
                            AddNodeScreen(
                                tripId = tripId,
                                viewModel = tripViewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = Screen.InviteParticipant.route,
                            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
                            InviteParticipantScreen(
                                tripId = tripId,
                                viewModel = tripViewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = Screen.GroupBalance.route,
                            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
                        ) {
                            GroupBalanceScreen(
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Expenses.route) {
                            ExpensesScreen(
                                viewModel = expensesViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
    selectedTextColor = MaterialTheme.colorScheme.onSurface,
    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
)

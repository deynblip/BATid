package com.batid.app.ui.navigation
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.batid.app.data.network.ConnectivityObserver
import com.batid.app.ui.admin.AdminRoot
import com.batid.app.ui.auth.AdminSignInScreen
import com.batid.app.ui.auth.CreateAccountScreen
import com.batid.app.ui.auth.SplashScreen
import com.batid.app.ui.auth.StudentSignInScreen
import com.batid.app.ui.auth.WelcomeScreen
import com.batid.app.ui.components.LocalIsOnline
import com.batid.app.ui.student.StudentRoot
import com.batid.app.data.model.roleEnum
import com.batid.app.data.remote.AuthRepository

@Composable
fun BatidApp() {
    val context = LocalContext.current
    val observer = remember { ConnectivityObserver(context) }
    val isOnline by observer.isOnline.collectAsStateWithLifecycle(initialValue = observer.isCurrentlyOnline())
    val navController = rememberNavController()

    CompositionLocalProvider(LocalIsOnline provides isOnline) {
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(onFinished = {
                    fun go(route: String) {
                        navController.navigate(route) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                    if (AuthRepository.isSignedIn) {
                        // May naka-login pa sa Firebase. Kunin ang profile para malaman ang role.
                        AuthRepository.loadCurrentProfile { profile ->
                            when {
                                profile == null -> go(Routes.WELCOME)
                                profile.roleEnum.canPublish -> go(Routes.ADMIN_ROOT)
                                else -> go(Routes.STUDENT_ROOT)
                            }
                        }
                    } else {
                        go(Routes.WELCOME)
                    }
                })
            }
            composable(Routes.WELCOME) {
                WelcomeScreen(
                    onLogIn = { navController.navigate(Routes.STUDENT_SIGN_IN) },
                    onCreateAccount = { navController.navigate(Routes.CREATE_ACCOUNT) }
                )
            }
            composable(Routes.STUDENT_SIGN_IN) {
                StudentSignInScreen(
                    onBack = { navController.popBackStack() },
                    onSignedIn = { navController.enterRoot(Routes.STUDENT_ROOT) },
                    onAdminSignIn = { navController.navigate(Routes.ADMIN_SIGN_IN) }
                )
            }
            composable(Routes.CREATE_ACCOUNT) {
                CreateAccountScreen(
                    onBack = { navController.popBackStack() },
                    onCreated = {
                        Toast.makeText(context, "Account created. Please log in.", Toast.LENGTH_SHORT).show()
                        navController.navigate(Routes.STUDENT_SIGN_IN) { popUpTo(Routes.WELCOME) }
                    },
                    onLogIn = {
                        navController.navigate(Routes.STUDENT_SIGN_IN) { popUpTo(Routes.WELCOME) }
                    }
                )
            }
            composable(Routes.ADMIN_SIGN_IN) {
                AdminSignInScreen(
                    onBack = { navController.popBackStack() },
                    onSignedIn = { navController.enterRoot(Routes.ADMIN_ROOT) }
                )
            }
            composable(Routes.STUDENT_ROOT) {
                StudentRoot(onSignOut = { navController.signOutToWelcome() })
            }
            composable(Routes.ADMIN_ROOT) { AdminRoot(onSignOut = {
                    AuthRepository.signOut()
                    navController.signOutToWelcome()
                })
            }
        }
    }
}

/** Go to a role root and clear the auth screens from the back stack. */
private fun NavController.enterRoot(route: String) {
    navigate(route) { popUpTo(graph.id) { inclusive = true } }
}

private fun NavController.signOutToWelcome() {
    navigate(Routes.WELCOME) { popUpTo(graph.id) { inclusive = true } }
}

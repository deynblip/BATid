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
import com.batid.app.ui.auth.CompleteProfileScreen
import com.batid.app.ui.auth.CreateAccountScreen
import com.batid.app.ui.auth.SplashScreen
import com.batid.app.ui.auth.StudentSignInScreen
import com.batid.app.ui.auth.VerifyEmailScreen
import com.batid.app.ui.auth.WelcomeScreen
import com.batid.app.ui.components.LocalIsOnline
import com.batid.app.ui.student.StudentRoot
import com.batid.app.data.model.UserProfile
import com.batid.app.data.model.UserRole
import com.batid.app.data.model.roleEnum
import com.batid.app.data.remote.AuthRepository
import com.batid.app.notifications.NotificationPrefs
import com.batid.app.notifications.PushTopics

@Composable
fun BatidApp() {
    val context = LocalContext.current
    val observer = remember { ConnectivityObserver(context) }
    val isOnline by observer.isOnline.collectAsStateWithLifecycle(initialValue = observer.isCurrentlyOnline())
    val navController = rememberNavController()

    /** Where a signed-in account goes: verify email first, then its own side. */
    fun homeFor(profile: UserProfile): String = when {
        AuthRepository.needsEmailVerification(profile) -> Routes.VERIFY_EMAIL
        profile.roleEnum.usesAdminSide -> Routes.ADMIN_ROOT
        // Council: back to the last side used (student or dashboard).
        profile.roleEnum == UserRole.COUNCIL && SidePrefs.councilOnAdminSide(context) -> Routes.ADMIN_ROOT
        else -> Routes.STUDENT_ROOT
    }

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
                            if (profile == null) go(Routes.WELCOME) else go(homeFor(profile))
                        }
                    } else {
                        go(Routes.WELCOME)
                    }
                })
            }
            composable(Routes.WELCOME) {
                WelcomeScreen(
                    onLogIn = { navController.navigate(Routes.STUDENT_SIGN_IN) },
                    onCreateAccount = { navController.navigate(Routes.CREATE_ACCOUNT) },
                    onGoogleSignedIn = { needsProfile ->
                        val profile = AuthRepository.currentProfile
                        when {
                            needsProfile -> navController.enterRoot(Routes.COMPLETE_PROFILE)
                            profile != null -> navController.enterRoot(homeFor(profile))
                            else -> Unit
                        }
                    }
                )
            }
            composable(Routes.STUDENT_SIGN_IN) {
                StudentSignInScreen(
                    onBack = { navController.popBackStack() },
                    onSignedIn = { _ ->
                        val profile = AuthRepository.currentProfile
                        if (profile == null) navController.signOutToWelcome()
                        else navController.enterRoot(homeFor(profile))
                    }
                )
            }
            composable(Routes.CREATE_ACCOUNT) {
                CreateAccountScreen(
                    onBack = { navController.popBackStack() },
                    onCreated = {
                        Toast.makeText(context, "Account created. Check your email to verify it.", Toast.LENGTH_LONG).show()
                        navController.enterRoot(Routes.VERIFY_EMAIL)
                    },
                    onLogIn = {
                        navController.navigate(Routes.STUDENT_SIGN_IN) { popUpTo(Routes.WELCOME) }
                    }
                )
            }
            // Task 12: first Google sign-in, no BATid profile yet.
            composable(Routes.COMPLETE_PROFILE) {
                CompleteProfileScreen(
                    email = AuthRepository.currentEmail,
                    defaultName = AuthRepository.currentDisplayName,
                    onDone = {
                        val profile = AuthRepository.currentProfile
                        if (profile == null) navController.signOutToWelcome()
                        else navController.enterRoot(homeFor(profile))
                    },
                    onCancel = {
                        AuthRepository.signOut()
                        navController.signOutToWelcome()
                    }
                )
            }
            composable(Routes.VERIFY_EMAIL) {
                VerifyEmailScreen(
                    email = AuthRepository.currentEmail,
                    onVerified = {
                        val profile = AuthRepository.currentProfile
                        if (profile == null) navController.signOutToWelcome()
                        else navController.enterRoot(homeFor(profile))
                    },
                    onSignOut = {
                        AuthRepository.signOut()
                        navController.signOutToWelcome()
                    }
                )
            }
            composable(Routes.STUDENT_ROOT) {
                StudentRoot(
                    onSignOut = {
                        SidePrefs.setCouncilOnAdminSide(context, false)
                        navController.signOutToWelcome()
                    },
                    onOpenCouncilDashboard = {
                        SidePrefs.setCouncilOnAdminSide(context, true)
                        navController.enterRoot(Routes.ADMIN_ROOT)
                    }
                )
            }
            composable(Routes.ADMIN_ROOT) {
                AdminRoot(
                    onSignOut = {
                        SidePrefs.setCouncilOnAdminSide(context, false)
                        // Council is also a student: stop their pushes like the student side does.
                        PushTopics.clear()
                        NotificationPrefs.signOut(context)
                        AuthRepository.signOut()
                        navController.signOutToWelcome()
                    },
                    onStudentView = {
                        SidePrefs.setCouncilOnAdminSide(context, false)
                        navController.enterRoot(Routes.STUDENT_ROOT)
                    }
                )
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
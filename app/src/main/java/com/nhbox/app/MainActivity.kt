package com.nhbox.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhbox.app.ui.auth.AuthViewModel
import com.nhbox.app.ui.navigation.NhBoxNavGraph
import com.nhbox.app.ui.theme.NhBoxTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val authViewModel: AuthViewModel = hiltViewModel()
            val profile by authViewModel.userProfile.collectAsState()
            val darkTheme = profile?.preferences?.darkTheme ?: true

            NhBoxTheme(darkTheme = darkTheme) {
                NhBoxNavGraph(authViewModel = authViewModel)
            }
        }
    }
}

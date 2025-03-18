package com.example.xrexp.m3
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun DetailPane(navigator: ThreePaneScaffoldNavigator<Destination>) {
    val destination = navigator.currentDestination?.contentKey ?: return
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        topBar = { TopAppBar(title = { Text("XR Compose Adaptive: ${destination.label}") }) },
    ) { innerPadding ->
        Surface(Modifier.fillMaxSize().padding(innerPadding)) {
            when (destination) {
                Destination.Dialog -> DetailPaneDialog()
            }
        }
    }
    BackHandler { coroutineScope.launch { navigator.navigateBack() } }
}

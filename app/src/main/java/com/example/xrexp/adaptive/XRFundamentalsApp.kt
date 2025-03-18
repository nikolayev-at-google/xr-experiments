package com.example.xrexp.adaptive

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import com.example.xrexp.adaptive.component.PrimaryCard
import com.example.xrexp.adaptive.component.SecondaryCardList
import com.example.xrexp.adaptive.component.XRFundamentalsTopAppBar
import com.example.xrexp.adaptive.layout.CompactLayout
import com.example.xrexp.adaptive.layout.ExpandedLayout

@Composable
fun XRFundamentalsApp(
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
) {
    Scaffold(
        topBar = { XRFundamentalsTopAppBar() }
    ) { innerPadding ->

        val modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()

        if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT) {
            CompactLayout(
                modifier = modifier,
                primaryContent = {
                    PrimaryCard()
                },
                secondaryContent = {
                    SecondaryCardList()
                }
            )
        } else {
            ExpandedLayout(
                modifier = modifier,
                primaryContent = {
                    PrimaryCard(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                },
                secondaryContent = {
                    SecondaryCardList(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }
            )
        }
    }
}
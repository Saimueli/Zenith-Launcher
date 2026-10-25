package com.example.zenithlauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.zenithlauncher.ui.AppsScreen
import com.example.zenithlauncher.ui.DockBar
import com.example.zenithlauncher.ui.HabitsScreen
import com.example.zenithlauncher.ui.StatsScreen
import com.example.zenithlauncher.ui.TabItem
import com.example.zenithlauncher.ui.ToDoScreen
import com.example.zenithlauncher.ui.ZenithViewModel
import com.example.zenithlauncher.ui.theme.PureBlack
import com.example.zenithlauncher.ui.theme.ZenithLauncherTheme
import com.example.zenithlauncher.wallpaper.WallpaperUtil

class MainActivity : ComponentActivity() {
    private val viewModel: ZenithViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ZenithLauncherTheme {
                LaunchedEffect(Unit) {
                    WallpaperUtil.setBlackWallpaper(this@MainActivity)
                }

                MainContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainContent(viewModel: ZenithViewModel) {
    var currentTab by remember { mutableStateOf(TabItem.APPS) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = PureBlack,
        bottomBar = {
            DockBar(
                selectedTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PureBlack)
                .padding(innerPadding)
        ) {
            when (currentTab) {
                TabItem.APPS -> AppsScreen(viewModel = viewModel)
                TabItem.TODO -> ToDoScreen(viewModel = viewModel)
                TabItem.HABITS -> HabitsScreen(viewModel = viewModel)
                TabItem.STATS -> StatsScreen(viewModel = viewModel)
            }
        }
    }
}

package jp.tpp.t9s.sonicfix.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import jp.tpp.t9s.sonicfix.R
import jp.tpp.t9s.sonicfix.audio.AudioEngine
import jp.tpp.t9s.sonicfix.audio.AudioMicAnalyzer
import jp.tpp.t9s.sonicfix.audio.VibrationManager
import jp.tpp.t9s.sonicfix.billing.BillingManager
import jp.tpp.t9s.sonicfix.ui.components.BannerAdPlaceholder
import jp.tpp.t9s.sonicfix.ui.screens.AutoCleanScreen
import jp.tpp.t9s.sonicfix.ui.screens.DiagnosticsScreen
import jp.tpp.t9s.sonicfix.ui.screens.ManualGenScreen
import jp.tpp.t9s.sonicfix.ui.screens.ProUpgradeScreen
import jp.tpp.t9s.sonicfix.ui.theme.CyanPrimary
import jp.tpp.t9s.sonicfix.ui.theme.SurfaceDark

enum class MainTab(val titleRes: Int, val icon: ImageVector) {
    CLEAN(R.string.tab_clean, Icons.Default.WaterDrop),
    MANUAL(R.string.tab_manual, Icons.Default.Tune),
    DIAGNOSTICS(R.string.tab_test, Icons.Default.Build),
    PRO(R.string.tab_pro, Icons.Default.WorkspacePremium)
}

@Composable
fun MainScreen(
    audioEngine: AudioEngine,
    vibrationManager: VibrationManager,
    micAnalyzer: AudioMicAnalyzer,
    billingManager: BillingManager
) {
    var selectedTab by remember { mutableStateOf(MainTab.CLEAN) }
    val isPro by billingManager.isPro.collectAsState()

    Scaffold(
        bottomBar = {
            Column {
                BannerAdPlaceholder(
                    isPro = isPro,
                    onUpgradeClick = { selectedTab = MainTab.PRO }
                )
                NavigationBar(
                    containerColor = SurfaceDark,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ) {
                    MainTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (selectedTab != tab) {
                                    audioEngine.stop()
                                    vibrationManager.stop()
                                    micAnalyzer.stopAnalyzing()
                                }
                                selectedTab = tab
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = stringResource(tab.titleRes)
                                )
                            },
                            label = { Text(stringResource(tab.titleRes)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CyanPrimary,
                                selectedTextColor = CyanPrimary,
                                indicatorColor = CyanPrimary.copy(alpha = 0.15f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            when (selectedTab) {
                MainTab.CLEAN -> AutoCleanScreen(
                    audioEngine = audioEngine,
                    vibrationManager = vibrationManager
                )
                MainTab.MANUAL -> ManualGenScreen(
                    audioEngine = audioEngine,
                    vibrationManager = vibrationManager
                )
                MainTab.DIAGNOSTICS -> DiagnosticsScreen(
                    audioEngine = audioEngine,
                    micAnalyzer = micAnalyzer
                )
                MainTab.PRO -> ProUpgradeScreen(
                    billingManager = billingManager
                )
            }
        }
    }
}

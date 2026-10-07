package jp.tpp.t9s.sonicfix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import jp.tpp.t9s.sonicfix.audio.AudioEngine
import jp.tpp.t9s.sonicfix.audio.AudioMicAnalyzer
import jp.tpp.t9s.sonicfix.audio.VibrationManager
import jp.tpp.t9s.sonicfix.billing.BillingManager
import jp.tpp.t9s.sonicfix.ui.MainScreen
import jp.tpp.t9s.sonicfix.ui.theme.SonicFixTheme

class MainActivity : ComponentActivity() {

    private lateinit var audioEngine: AudioEngine
    private lateinit var vibrationManager: VibrationManager
    private lateinit var micAnalyzer: AudioMicAnalyzer
    private lateinit var billingManager: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        audioEngine = AudioEngine()
        vibrationManager = VibrationManager(this)
        micAnalyzer = AudioMicAnalyzer()
        billingManager = BillingManager(this)

        setContent {
            SonicFixTheme {
                MainScreen(
                    audioEngine = audioEngine,
                    vibrationManager = vibrationManager,
                    micAnalyzer = micAnalyzer,
                    billingManager = billingManager
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // バックグラウンド移行時は安全のため音と振動を停止
        audioEngine.stop()
        vibrationManager.stop()
        micAnalyzer.stopAnalyzing()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioEngine.stop()
        vibrationManager.stop()
        micAnalyzer.stopAnalyzing()
        billingManager.destroy()
    }
}

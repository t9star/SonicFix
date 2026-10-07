package jp.tpp.t9s.sonicfix

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.android.gms.ads.MobileAds
import jp.tpp.t9s.sonicfix.audio.AudioEngine
import jp.tpp.t9s.sonicfix.audio.AudioMicAnalyzer
import jp.tpp.t9s.sonicfix.audio.VibrationManager
import jp.tpp.t9s.sonicfix.billing.BillingManager
import jp.tpp.t9s.sonicfix.ui.MainScreen
import jp.tpp.t9s.sonicfix.ui.MainTab
import jp.tpp.t9s.sonicfix.ui.theme.SonicFixTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var audioEngine: AudioEngine
    private lateinit var vibrationManager: VibrationManager
    private lateinit var micAnalyzer: AudioMicAnalyzer
    private lateinit var billingManager: BillingManager

    private val demoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "jp.tpp.t9s.sonicfix.RELOAD_DEMO") {
                val locale = intent.getStringExtra("EXTRA_LOCALE")
                val tab = intent.getStringExtra("EXTRA_TAB")

                val prefs = getSharedPreferences("sonicfix_demo_prefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("demo_locale", locale ?: "")
                    .putString("demo_tab", tab ?: "")
                    .apply()

                recreate()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val filter = IntentFilter("jp.tpp.t9s.sonicfix.RELOAD_DEMO")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(demoReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(demoReceiver, filter)
        }

        val prefs = getSharedPreferences("sonicfix_demo_prefs", Context.MODE_PRIVATE)
        val targetLocale = prefs.getString("demo_locale", "")
        val targetTabStr = prefs.getString("demo_tab", "")

        if (!targetLocale.isNullOrEmpty()) {
            val locale = if (targetLocale.contains("-")) {
                val parts = targetLocale.split("-")
                Locale(parts[0], parts[1])
            } else {
                Locale(targetLocale)
            }
            Locale.setDefault(locale)
            val config = resources.configuration
            config.setLocale(locale)
            @Suppress("DEPRECATION")
            resources.updateConfiguration(config, resources.displayMetrics)
        }

        val initialTab = when (targetTabStr?.lowercase()) {
            "manual" -> MainTab.MANUAL
            "diag" -> MainTab.DIAGNOSTICS
            "pro" -> MainTab.PRO
            else -> MainTab.CLEAN
        }

        enableEdgeToEdge()

        MobileAds.initialize(this) {}

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
                    billingManager = billingManager,
                    initialTab = initialTab
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        audioEngine.stop()
        vibrationManager.stop()
        micAnalyzer.stopAnalyzing()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(demoReceiver)
        } catch (_: Exception) {}
        audioEngine.stop()
        vibrationManager.stop()
        micAnalyzer.stopAnalyzing()
        billingManager.destroy()
    }
}

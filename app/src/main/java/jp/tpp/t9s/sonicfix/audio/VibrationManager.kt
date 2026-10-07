package jp.tpp.t9s.sonicfix.audio

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class VibrationManager(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /**
     * 水抜き用の物理振動パルス（強いバーストと休止を繰り返す）
     */
    fun startCleanVibration() {
        vibrator?.let { v ->
            if (!v.hasVibrator()) return@let

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // タイミング: 100ms ON, 150ms OFF, 200ms ON, 100ms OFF
                val timings = longArrayOf(0, 150, 100, 200, 100)
                val amplitudes = intArrayOf(0, 255, 0, 255, 0)
                val effect = VibrationEffect.createWaveform(timings, amplitudes, 0)
                v.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val pattern = longArrayOf(0, 150, 100, 200, 100)
                @Suppress("DEPRECATION")
                v.vibrate(pattern, 0)
            }
        }
    }

    /**
     * タップ時などのハプティクスフィードバック
     */
    fun tick() {
        vibrator?.let { v ->
            if (!v.hasVibrator()) return@let
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(20)
            }
        }
    }

    fun stop() {
        vibrator?.cancel()
    }
}

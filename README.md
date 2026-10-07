# SonicFix 🌊🔊

**SonicFix** is a lightweight, zero-maintenance (100% offline, $0 running cost) Android utility app designed for the global smartphone market (India, Brazil, Indonesia, United States, Mexico, Japan, and worldwide).

It utilizes acoustic physics—resonant frequency sweeps (165Hz – 1,200Hz) paired with synchronized micro-vibrations—to physically expel trapped water droplets and fine dust particles from smartphone speakers.

---

## 🌟 Key Features

1. **🌊 Auto Speaker Water Eject & Dust Purge**
   - 35-second automated resonant acoustic sweep tailored to the mechanical resonance of smartphone speaker cones.
   - Dynamic sine-wave pulse modulation with rhythmic hardware vibration bursts.
   - Dynamic wave visualizer & circular progress indicator with countdown.

2. **🎛️ Manual Frequency & Tone Generator**
   - 20 Hz to 20,000 Hz continuous high-precision frequency slider & +/- step controls.
   - Multiple waveform types: **Sine**, **Square**, **Triangle**.
   - One-tap presets: *Eject (165Hz)*, *Bass (80Hz)*, *A4 Reference (440Hz)*, *Ultrasonic (15kHz)*.

3. **🎙️ Audio & Hardware Diagnostics**
   - Stereo separation test: Independent Left / Right / Stereo channel playback.
   - Frequency band verification: Bass (80Hz), Midrange (1kHz), Treble (10kHz).
   - Real-time microphone sound pressure meter (dB) for verifying sound restoration.

4. **🌍 True Global Multilingual Support (6 Languages)**
   - 🇺🇸 English (Default)
   - 🇮🇳 Hindi (India - #1 Android download market)
   - 🇧🇷 Portuguese (Brazil - #2 Android download market)
   - 🇮🇩 Indonesian (Indonesia - #3 Android download market)
   - 🇲🇽 Spanish (Mexico & Latin America - #5 Android download market)
   - 🇯🇵 Japanese

5. **💎 Monetization Ready (Zero Running Cost)**
   - **Google Play Billing v6.2.1** integration (`sonicfix_pro_lifetime`).
   - Clean non-intrusive bottom banner ads for free users.
   - Completion interstitial ad triggers.
   - One-time Pro purchase unlock (Ad-free, deep purge, full-band tools).

---

## 🛠️ Architecture & Tech Stack

- **UI Framework**: Modern declarative Jetpack Compose with Material 3 (dark-navy high-contrast cyber theme).
- **Audio Engine**: Low-latency PCM stream generation using native Android `AudioTrack` API.
- **Haptics**: Android `Vibrator` & `VibratorManager` (Android 12+ / S API 31 support).
- **Billing**: Official Google Play Billing Client 6.2.1 (`com.android.billingclient:billing-ktx:6.2.1`).
- **Package ID**: `jp.tpp.t9s.sonicfix`
- **Target SDK**: Android 16 (API 36), **Min SDK**: Android 7.0 (API 24).

---

## 📦 How to Build

Open project in Android Studio or compile via Gradle:

```bash
# Debug APK build
./gradlew assembleDebug

# Release Bundle (.aab) build
./gradlew bundleRelease
```

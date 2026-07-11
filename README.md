<div align="center">

# ⌨️ ModernKey

**A feature-rich custom Android keyboard with stylish fonts, emoji, voice input & clipboard manager**

[![Build APK](https://github.com/your-username/ModernKey/actions/workflows/build.yml/badge.svg)](https://github.com/your-username/ModernKey/actions/workflows/build.yml)
![Android](https://img.shields.io/badge/Android-26%2B-brightgreen?logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9-purple?logo=kotlin)
![License](https://img.shields.io/badge/License-MIT-blue)

</div>

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| 🔤 **15 Font Styles** | Bold, Italic, Script, Fraktur, Monospace, Circled, Double-struck & more |
| 😀 **Emoji Panel** | 200+ emojis with search, categories & recent history |
| 🎙️ **Voice Input** | Speech-to-text in multiple languages including Bengali |
| 📋 **Clipboard Manager** | Auto-saves copied text, pin favourites, quick paste |
| 🎨 **7 Themes** | Light Classic, Dark Mode, Midnight Blue, Sunset Pink & more |
| ⚡ **QWERTY Layout** | Smooth Canvas-drawn keys with haptic feedback |

---

## 📱 Screenshots

> *Coming soon — build the APK and install to see ModernKey in action!*

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK API 35
- Android device / emulator running API 26+

### Clone & Build
```bash
git clone https://github.com/your-username/ModernKey.git
cd ModernKey
./gradlew assembleDebug
```
The APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

### Install via ADB
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## ⚙️ Enable the Keyboard

1. **Settings** → **General Management** → **Keyboard list and default**
2. Enable **ModernKey**
3. Set as **default keyboard**
4. Open any app and start typing!

---

## 🏗️ Architecture

```
com.modernkey.keyboard
├── ime/                    ← InputMethodService (core)
├── ui/
│   ├── keyboard/           ← KeyboardView (Canvas), FontBarView
│   ├── emoji/              ← EmojiPanelView, adapters
│   ├── voice/              ← VoiceOverlayView
│   └── settings/           ← SettingsActivity
├── font/                   ← FontStyle, FontConverter, FontCharMaps
├── voice/                  ← VoiceInputManager (SpeechRecognizer)
├── emoji/                  ← EmojiData, EmojiRepository
├── clipboard/              ← ClipboardItem, DAO, Repository
└── data/
    ├── db/                 ← AppDatabase (Room)
    └── prefs/              ← KeyboardPreferences
```

### Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | **Kotlin 1.9** |
| Build | **Gradle 8.7 + AGP 8.4** |
| Database | **Room 2.6** |
| Async | **Coroutines + Flow** |
| UI | **Custom Canvas Views** |
| Voice | **Android SpeechRecognizer** |
| CI/CD | **GitHub Actions** |

---

## 🤖 GitHub Actions CI/CD

Every push to `main` automatically:
1. ✅ Compiles the project with JDK 17
2. ✅ Runs unit tests
3. ✅ Builds a debug APK
4. ✅ Uploads APK as a downloadable artifact

Download the latest APK from the [Actions tab](../../actions) → latest workflow run → **Artifacts**.

---

## 🎨 Font Styles

| Style | Preview | Description |
|-------|---------|-------------|
| Normal | Abc | Default system font |
| Bold | 𝐀𝐛𝐜 | Mathematical bold serif |
| Italic | 𝐴𝑏𝑐 | Mathematical italic serif |
| Bold Italic | 𝑨𝒃𝒄 | Mathematical bold italic |
| Script | 𝒜𝒷𝒸 | Mathematical script |
| Bold Script | 𝓐𝓫𝓬 | Mathematical bold script |
| Fraktur | 𝔄𝔟𝔠 | Mathematical fraktur |
| Double Struck | 𝔸𝕓𝕔 | Mathematical double-struck |
| Monospace | 𝙰𝚋𝚌 | Mathematical monospace |
| Circled | Ⓐⓑⓒ | Enclosed alphanumerics |
| Squared | 🅰bc | Enclosed alphanumeric supplement |
| Tiny Caps | ᴀʙᴄ | Phonetic small capitals |
| Upside Down | ɐqɔ | Flipped characters |
| Strikethrough | A̶b̶c̶ | Combining strikethrough |
| Underline | A͟b͟c͟ | Combining underline |

---

## 📝 License

```
MIT License — feel free to use, modify & distribute.
```

---

<div align="center">
Made with ❤️ using Kotlin & Android SDK
</div>

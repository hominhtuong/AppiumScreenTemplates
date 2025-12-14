# AppiumScreenTemplates

[![Version](https://img.shields.io/jetbrains/plugin/v/29362.svg)](https://plugins.jetbrains.com/plugin/25300)
[![Rating](https://img.shields.io/jetbrains/plugin/r/rating/29362.svg)](https://plugins.jetbrains.com/plugin/25300)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/29362.svg)](https://plugins.jetbrains.com/plugin/25300)

---

## 🚀 Overview

**AppiumScreenTemplates** is an IntelliJ IDEA plugin that helps Appium automation engineers quickly generate **Screen (Page Object)** and **Test** classes based on predefined templates.

By simply entering a screen name, the plugin automatically creates the corresponding Java files with a consistent structure, saving time and avoiding repetitive boilerplate code.

This plugin is designed for teams using **Appium + Java** who want a clean, maintainable, and repeatable test architecture.

---

## ✨ Key Features

- 🧩 Generate **Appium Screen (Page Object)** classes instantly
- 🧪 Optionally generate matching **Test** classes
- 📁 Auto-create missing folders (e.g. `src/test/java` and package paths)
- 🧠 Smart naming:
    - PascalCase for class names
    - camelCase for variables
- ⚠️ Duplicate file detection with in-dialog warning
- 🛡 Safe file creation using IntelliJ Write Actions (no IDE crash)

---

## 🧑‍💻 How It Works

1. Right-click on a target directory in the **Project View**
2. Select **New → New Screen Class**
3. Enter a screen name (e.g. `Login`, `Order Detail`, `HomeScreen`)
4. Optionally enable **“Also create test class”**
5. Click **OK**

The plugin will generate:
- `LoginScreen.java`
- `LoginTest.java` (optional)

If a file with the same name already exists, the plugin will show a warning **inside the dialog** and prevent accidental overwrites.

---

## ⌨️ Shortcut

- **Windows / Linux:** `Ctrl + Alt + N`
- **macOS:** `⌘ + ⌥ + N`

---

## 🧱 Requirements

- IntelliJ IDEA (Community or Ultimate)
- Java 17+
- Project using **Appium + Java**

> ⚠️ The plugin does not rely on experimental Java features and works safely with stable JDK versions.

---

## 🆕 Changelog

### 1.0.1
- Add warning when creating Screen or Test files with duplicate names
- Prevent accidental overwrite by showing errors inside the creation dialog
- Improve stability by ensuring all file operations run inside Write Actions

### 1.0.0
- Initial release
- Generate Appium Screen (Page Object) classes from predefined templates
- Optionally generate matching Test classes
- Smart naming conversion (PascalCase for classes, camelCase for variables)
- Basic project setup and template initialization

---

## 📝 License

This project is proprietary software.  
All rights reserved.

© 2025 MiTu Ultra

---

## 💬 Feedback & Support

If you encounter issues, have questions, or want to request new features, please feel free to reach out.

- 📧 **Email:** support@mituultra.com
- 🌐 **Website:** https://mituultra.com

You can also open an issue on this repository for bug reports or feature requests.

**Made with ❤️ by MiTu Ultra**



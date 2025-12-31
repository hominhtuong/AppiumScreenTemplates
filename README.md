# Appium Screen Templates

[![Version](https://img.shields.io/jetbrains/plugin/v/29362.svg)](https://plugins.jetbrains.com/plugin/25300)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/29362.svg)](https://plugins.jetbrains.com/plugin/25300)

---

## 🚀 Overview

**Appium Screen Templates** is an IntelliJ IDEA plugin that helps Appium automation engineers quickly generate **Screen (Page Object)** and **Test** classes based on predefined templates.

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

### 2.0.0
- The plugin uses a unified `@MobileFindBy` annotation for both Android and iOS:
```java
@MobileFindBy(id = "backButton")
public WebElement backButton;
```

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

## Customization Guide for Your Project

If you want to fork this source code to build your own plugin, follow these steps:

### 1. Change Plugin Information

**File:** `src/main/resources/META-INF/plugin.xml`

```xml
<id>com.yourcompany.appium.templates</id>
<name>Your Company Appium Templates</name>
<vendor url="https://yourcompany.com">Your Company</vendor>
```

### 2. Change Test Package Path

**File:** `src/main/kotlin/com/mitu/appium/template/MiTuScreenAction.kt`

Find line 20:
```kotlin
private val testPackagePath = "com/dt/tests"
```

Change to your company's package path:
```kotlin
private val testPackagePath = "com/yourcompany/tests"
```

> **Note:** You also need to update the package in `MiTuTest.java.ft` template file (see step 3).

### 3. Customize File Templates

#### Screen Template

**File:** `src/main/resources/fileTemplates/internal/MiTuScreen.java.ft`

#### Test Template

**File:** `src/main/resources/fileTemplates/internal/MiTuTest.java.ft`


#### Template Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `${PACKAGE_NAME}` | Package of selected directory | `com.yourcompany.screens` |
| `${CLASS_NAME}` | Class name (Screen or Test) | `HomeScreen`, `HomeTest` |
| `${SCREEN_CLASS}` | Screen class name (Test only) | `HomeScreen` |
| `${VARIABLE_NAME}` | Variable name (camelCase) | `homeScreen` |
| `${BASE_NAME}` | Original name entered by user | `Home` |
| `${BASE_NAME_LOWER}` | Base name with lowercase first char | `home` |
| `${AUTHOR}` | System username | `admin` |
| `${CREATED}` | Creation date | `31 Dec, 2024` |

### 4. Change Plugin Icon

**Directory:** `src/main/resources/icons/`
- `actionIcon.svg` - Icon displayed in action menu
- `META-INF/pluginIcon.svg` - Icon displayed in Plugin Marketplace (40x40)
- `META-INF/pluginIcon_dark.svg` - Icon for dark theme


### 5. Change Keyboard Shortcut

**File:** `src/main/resources/META-INF/plugin.xml`

```xml
<keyboard-shortcut keymap="$default" first-keystroke="ctrl alt N"/>
<keyboard-shortcut keymap="Mac OS X" first-keystroke="meta alt N"/>
```

Replace `ctrl alt N` and `meta alt N` with your preferred shortcut.

### 6. Change Action Name in Menu

**File:** `src/main/resources/META-INF/plugin.xml`

```xml
<action
    id="com.yourcompany.appium.template.action.ScreenAction"
    class="com.mitu.appium.template.MiTuScreenAction"
    text="New Screen Class"
    description="Create Appium Screen and optional Test class"
    icon="/icons/actionIcon.svg"/>
```

- `text`: Name displayed in **New** menu
- `description`: Description shown on hover


### 7. Build and Distribute

```bash
  #Build plugin:
  ./gradlew buildPlugin

# Output file
ls build/distributions/
# → mitu_appium_template-2.0.2.zip
```

Install plugin:
1. Open IntelliJ IDEA
2. **Settings → Plugins → ⚙️ → Install Plugin from Disk...**
3. Select the `.zip` file you just built

---

## Project Structure

```
src/main/
├── kotlin/com/mitu/appium/template/
│   ├── MiTuScreenAction.kt      # Main logic for file creation
│   └── MiTuScreenDialog.kt      # Dialog for Screen name input
├── resources/
│   ├── META-INF/plugin.xml      # Plugin configuration
│   ├── fileTemplates/internal/
│   │   ├── MiTuScreen.java.ft   # Screen class template
│   │   └── MiTuTest.java.ft     # Test class template
│   └── icons/                   # Icons
```

## 💬 Feedback & Support

If you encounter issues, have questions, or want to request new features, please feel free to reach out.

- 📧 **Email:** support@mituultra.com
- 🌐 **Website:** https://mituultra.com

You can also open an issue on this repository for bug reports or feature requests.

**Made with ❤️ by MiTu Ultra**



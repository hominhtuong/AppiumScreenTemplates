plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.7.1"
}

group = "com.mitu.appium"
version = "1.0.3"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Configure IntelliJ Platform Gradle Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    intellijPlatform {
        create("IC", "2025.1.4.1")
        bundledPlugin("com.intellij.java")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        name = "MiTu Appium Templates"
        version = project.version.toString()

        vendor {
            name = "MiTu"
            email = "support@mituultra.com"
        }
        ideaVersion {
            sinceBuild = "251"
        }

        description = """
            This plugin helps Appium automation engineers quickly generate screen and test files based on predefined templates.
By entering a screen name, the plugin automatically creates corresponding screen and test classes, ensuring consistent structure and faster test development.
        """.trimIndent()

        changeNotes = """
                <li>Add warning when a screen or test file with the same name already exists.</li> 
        """.trimIndent()
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
